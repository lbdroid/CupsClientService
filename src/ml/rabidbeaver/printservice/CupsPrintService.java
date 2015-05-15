package ml.rabidbeaver.printservice;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.tasks.PrintTask;
import ml.rabidbeaver.tasks.PrintTaskListener;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsPrintJob;
import ml.rabidbeaver.cupsjni.JobOptions;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintJobInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

public class CupsPrintService extends PrintService implements PrintTaskListener{

	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		return new CupsPrinterDiscoverySession(this);
	}

	@Override
	protected void onPrintJobQueued(PrintJob job) {
		PrintJobInfo jobInfo = job.getInfo();
		
		boolean advancedresolution = false;
		
	    String nickname = jobInfo.getPrinterId().getLocalId();
	    PrintQueueConfHandler dbconf = new PrintQueueConfHandler(CupsPrintApp.getContext());
		PrintQueueConfig config = dbconf.getPrinter(nickname);
		dbconf.close();
		if (config == null){
			job.fail("Printer Config not found");
			return;
		}

		List<JobOptions> cupsAttributes = new ArrayList<JobOptions>();
		
		List<JobOptions> printerAttributes = config.getPrinterAttributes();
		for (int i=0; i< printerAttributes.size(); i++){
			JobOptions opt = printerAttributes.get(i);
			if (opt.name.equals("job-settable-attributes-supported") && job.hasAdvancedOption(opt.value)){
				if (opt.value.equals("printer-resolution")) advancedresolution = true;
				cupsAttributes.add(new JobOptions(opt.value,job.getAdvancedStringOption(opt.value)));
			}
		}
		
		int copies = jobInfo.getCopies();
		cupsAttributes.add(new JobOptions("copies",Integer.toString(copies)));
		PageRange[] ranges = jobInfo.getPages();
		String rangeStr = "";
		for (PageRange range: ranges){
			int start = range.getStart() + 1;
			if (start < 1) start = 1;

			int end = range.getEnd() + 1;
			if (end > 65535 || end < 1) end = 65535;

			String rangeTmp;
			String from = String.valueOf(start);
			String to   = String.valueOf(end);
			if (from.equals(to)) rangeTmp = from;
			else rangeTmp = from + "-" + to;

			if (rangeStr.equals("")) rangeStr = rangeTmp;
			else rangeStr = rangeStr + "," + rangeTmp;
		}
		if (rangeStr.equals("1-65535")) rangeStr = "";
		if (!(rangeStr.equals(""))) cupsAttributes.add(new JobOptions("page-ranges",rangeStr));

		PrintAttributes attributes = jobInfo.getAttributes();
		MediaSize mediaSize = attributes.getMediaSize();
		String tmp = mediaSize.getId();

		// TODO: pageSize == media?
		cupsAttributes.add(new JobOptions("media",tmp));
		if (mediaSize.isPortrait()) cupsAttributes.add(new JobOptions("orientation-requested","3"));
		else cupsAttributes.add(new JobOptions("orientation-requested","4"));

		// TODO: figure out the correct format for resolution
		if (!advancedresolution) cupsAttributes.add(new JobOptions("printer-resolution",attributes.getResolution().getHorizontalDpi()+"x"+attributes.getResolution().getVerticalDpi()));
		cupsAttributes.add(new JobOptions("print-color-mode",attributes.getColorMode()==PrintAttributes.COLOR_MODE_COLOR?"color":"monochrome"));

		PrintDocument document = job.getDocument();
		FileInputStream file = new ParcelFileDescriptor.AutoCloseInputStream(document.getData());
		String fileName = document.getInfo().getName();
		cupsAttributes.add(new JobOptions("job-name",fileName));
	    CupsPrintJob cupsPrintJob = new CupsPrintJob(file, fileName);
    	cupsPrintJob.setAttributes(cupsAttributes);

	    CupsClient cupsClient = new CupsClient(config.host,Integer.parseInt(config.port));
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

	@Override
	protected void onRequestCancelPrintJob(PrintJob job) {}
	
	@Override
	public void onPrintTaskDone(PrintTask task) {
		
		Exception exception = task.getException();
		PrintJob servicePrintJob = task.getServicePrintJob();
		String jobname = servicePrintJob.getDocument().getInfo().getName();
		String msg = "CupsPrint " + jobname + ": ";
		
		if (exception != null){
			Log.e("ONPRINTTASKDONE", msg + exception.getMessage());
			Toast.makeText(this, msg + "FAILED WITH EXCEPTION!!!", Toast.LENGTH_LONG).show();
			servicePrintJob.cancel();
			return;
		}
		
		int result = task.getResult();
		if (result == 0){
			Toast.makeText(this, msg + "Error unable to create print job.", Toast.LENGTH_LONG).show();
			servicePrintJob.cancel();
	    	return;
	    }
		
		Toast.makeText(this, msg+"Success", Toast.LENGTH_LONG).show();
		servicePrintJob.complete();
	}
}
