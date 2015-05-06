package ml.rabidbeaver.printservice;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.tasks.PrintTask;
import ml.rabidbeaver.tasks.PrintTaskListener;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsjni.CupsPrintJob;
import android.annotation.SuppressLint;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;


public class RBPrintService extends PrintService implements PrintTaskListener{
	
	static ConcurrentHashMap<String, CupsPpd> capabilities;
	static CupsPpd jobPpd;
	static PrintJobId ppdJobId;

	@SuppressLint("SdCardPath")
	@Override
	public void onCreate(){
		super.onCreate();
		Log.d("CUPSCLIENT-SETUPDIRS","WTF????!?!");
		for (String path: new String[]{"/data/data/ml.rabidbeaver.cupsprintservice/files/cache","/data/data/ml.rabidbeaver.cupsprintservice/files/tmp","/data/data/ml.rabidbeaver.cupsprintservice/files/logs"}){;
			File directory = new File(path);
			if (!directory.exists()) directory.mkdirs();
		}
		capabilities = new ConcurrentHashMap<String, CupsPpd>();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	
	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		return new RBPrinterDiscoverySession(this);
	}

	@Override
	protected void onPrintJobQueued(PrintJob job) {
		//job.start();
		String cupsString = "";
		String advString = job.getAdvancedStringOption("CupsString");
		if (advString == null){
			advString = "";
		}
		if (advString.equals("")){
			advString = "fit-to-page:boolean:true";
		}
		cupsString = addAttribute(cupsString, advString);

		PrintJobInfo jobInfo = job.getInfo();
		int copies = jobInfo.getCopies();
		cupsString = addAttribute(cupsString, "copies:integer:" + String.valueOf(copies));
		PageRange[] ranges = jobInfo.getPages();
		String rangeStr = "";
		for (PageRange range: ranges){
			int start = range.getStart() + 1;
			if (start < 1){
				start = 1;
			}
			int end = range.getEnd() + 1;
			if (end > 65535 || end < 1){
				end = 65535;
			}
			String rangeTmp;
			String from = String.valueOf(start);
			String to   = String.valueOf(end);
			if (from.equals(to)){
				rangeTmp = from;
			}
			else {
				rangeTmp = from + "-" + to;
			}
			if (rangeStr.equals("")){
				rangeStr = rangeTmp;
			}
			else {
				rangeStr = rangeStr + "," + rangeTmp;
			}
			
		}
		//rangeStr = "1-4";
		if (rangeStr.equals("1-65535")){
			rangeStr = "";
		}
		if (!(rangeStr.equals(""))){
			cupsString = addAttribute(cupsString, "page-ranges:setOfRangeOfInteger:" + rangeStr);
		}
		PrintAttributes attributes = jobInfo.getAttributes();
		MediaSize mediaSize = attributes.getMediaSize();
		String tmp = mediaSize.getId();
		cupsString = addAttribute(cupsString, "PageSize:keyword:"+ tmp);
		if (mediaSize.isPortrait()){
			cupsString = addAttribute(cupsString, "orientation-requested:enum:3");
		}
		else {
			cupsString = addAttribute(cupsString, "orientation-requested:enum:4");
		}
		//Resolution resolution = attributes.getResolution();
		//int colorModel = attributes.getColorMode();
		Map<String, String>cupsAttributes = null;
		if (!(cupsString.equals(""))){
			cupsAttributes = new HashMap<String, String>();
			cupsAttributes.put("job-attributes", cupsString);
		}

		PrintDocument document = job.getDocument();
		FileInputStream file = new ParcelFileDescriptor.AutoCloseInputStream(document.getData());
		String fileName = document.getInfo().getName();
	    CupsPrintJob cupsPrintJob = new CupsPrintJob(file, fileName);
	    if (attributes != null){
	    	cupsPrintJob.setAttributes(cupsAttributes);
	    }
	    
	    //TODO: This set of attributes needs to be handed to the CupsPrintJob as job attributes
	    String[] keys = new String[100];
	    cupsAttributes.keySet().toArray(keys);
	    for (int i=0; i<cupsAttributes.size(); i++){
	    	Log.d("RBPRINTSERVICE",keys[i]+"/"+cupsAttributes.get(keys[i]));
	    }
	    
	    String nickname = jobInfo.getPrinterId().getLocalId();
	    PrintQueueConfHandler dbconf = new PrintQueueConfHandler(CupsPrintApp.getContext());
		PrintQueueConfig config = dbconf.getPrinter(nickname);
		dbconf.close();
		if (config == null){
			job.fail("Printer Config not found");
			return;
		}
	    URL clientURL;
	    try {
	    	clientURL = new URL(config.getClient());
	    }catch (Exception e){
	    	System.err.println(e.toString());
	    	job.fail("Invalid print queue: " + config.getClient());
	    	return;
	    }
	    CupsClient cupsClient = new CupsClient(clientURL.getHost(),clientURL.getPort());
	    if (!(config.getPassword().equals(""))){
	    	cupsClient.setUserPass(config.getUserName(), config.getPassword());
	    }
	    PrintTask printTask = new PrintTask(cupsClient, config.queue);
		printTask.setJob(cupsPrintJob);
		printTask.setServicePrintJob(job);
		printTask.setListener(this);
		job.start();
		printTask.execute();
	}
	
	private String addAttribute(String cupsString, String attribute){
		
		if (attribute == null){
			return cupsString;
		}
	
		if (attribute.equals("")){
			return cupsString;
		}
		
		if (cupsString.equals("")){
			return attribute;
		}
		return cupsString + "#" + attribute;
	}

	@Override
	protected void onRequestCancelPrintJob(PrintJob job) {
	}
	
	//@Override
	public void onPrintTaskDone(PrintTask task) {
		
		Exception exception = task.getException();
		PrintJob servicePrintJob = task.getServicePrintJob();
		String jobname = servicePrintJob.getDocument().getInfo().getName();
		String errmsg = "CupsPrint " + jobname + ":\n";
		
		if (exception != null){
			Toast.makeText(this, errmsg + exception.toString(), Toast.LENGTH_LONG).show();
			servicePrintJob.cancel();
			return;
		}
		
		int result = task.getResult();
		if (result == 0){
			Toast.makeText(this, errmsg + "Print job returned 0", Toast.LENGTH_LONG).show();
			servicePrintJob.cancel();
	    	return;
	    }
		
		Toast.makeText(this, errmsg, Toast.LENGTH_LONG).show();
		servicePrintJob.complete();
	}
}
