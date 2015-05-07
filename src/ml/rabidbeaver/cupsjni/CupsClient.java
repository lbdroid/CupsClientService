package ml.rabidbeaver.cupsjni;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Map;

import android.util.Log;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.cups_password_cb_t;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.http_encryption_e;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.http_status_e;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.ipp_op_e;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.ipp_tag_e;
import ml.rabidbeaver.jna.cups_dest_s;
import ml.rabidbeaver.jna.cups_job_s;
import ml.rabidbeaver.jna.cups_option_s;

public class CupsClient {
	private String userName = "anonymous";
	private String password;
	private PointerByReference serv_conn_p;
	private IntBuffer i_b = IntBuffer.allocate(1);
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	private MlRabidbeaverJnaLibrary cups = MlRabidbeaverJnaLibrary.INSTANCE;
	public final int CUPS_WHICHJOBS_ACTIVE = MlRabidbeaverJnaLibrary.CUPS_WHICHJOBS_ACTIVE;
	
	// Constructors
	public CupsClient(String host, int port, String userName){
		this.userName=userName;
		serv_conn_p = cups.httpConnect2(host, port, null, 0, http_encryption_e.HTTP_ENCRYPTION_IF_REQUESTED, 0, 2500, i_b);
	}
	public CupsClient(String host, int port){
		serv_conn_p = cups.httpConnect(host, port);
	}
	public CupsClient(){}
	
	public void setUserPass(String user, String pass){
		this.userName=user;
		this.password=pass;
		if (userName != null && userName.length() > 0 && password != null && password.length() > 0){
			cups.cupsSetPasswordCB(new cups_password_cb_t(){
				@Override
				public String apply(Pointer prompt) {
					cups.cupsSetUser(userName);
					return password;
				}
			});
		}
	}
	
	public boolean isPrinterAccessible(String queue){
		if (getPrinter(queue) == null) return false;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public cups_job_s[] getJobs(String queue, int whichJobs, boolean myJobs){    
		Log.d("CUPSCLIENT-GETJOBS","STARTING");

		PointerByReference p = new PointerByReference();
		Pointer q = new Memory(queue.length()+1);
		q.setString(0, queue);
		//p.
		
		//cups_job_s.ByReference[] jobs = new cups_job_s.ByReference[100];
		//for (int i=0; i<100; i++) jobs[i] = new cups_job_s.ByReference(){};
		
		//cups.cupsGetJobs2
		int num = cups.cupsGetJobs2(serv_conn_p.getPointer(), p, q, myJobs?1:0, MlRabidbeaverJnaLibrary.CUPS_WHICHJOBS_ALL);//whichJobs);
		
		Log.d("CUPSCLIENT-GETJOBS","NUM:"+num);
		
		//cups_job_s j = new cups_job_s(p.getValue()){};
		Log.d("CUPSCLIENT-GETJOBS","1");

		Log.d("CUPSCLIENT-GETJOBS","2");

		Pointer ptr = p.getValue();
		Log.d("CUPSCLIENT-GETJOBS","2a");
		cups_job_s cjob = new cups_job_s(ptr){};
		Log.d("CUPSCLIENT-GETJOBS","2b");
		cjob.read();
		Log.d("CUPSCLIENT-GETJOBS","2c");
		cups_job_s[] jobarr = (cups_job_s[])cjob.toArray(num);
		
		//Pointer[] parr = p.getValue().getPointerArray(0,num);
		Log.d("CUPSCLIENT-GETJOBS","3");
		//jarr = new cups_job_s[num];
		
		
		
		Log.d("CUPSCLIENT-GETJOBS","4");
		for (int i=0; i<num; i++){
			Log.d("CUPSCLIENT-GETJOBS","5");
			//Pointer pt = jarr[i].getPointer();
			//pt = parr[i];
			
			//jarr[i] = new cups_job_s(parr[i].getPointer(0)){};
			Log.d("CUPSCLIENT-GETJOBS",jobarr[i].title.getString(0));
		}
		
		Log.d("CUPSCLIENT-GETJOBS","ABOUT TO EXIT");
		
		return jobarr;
		//Log.d("CUPSCLIENT-GETJOBS",""+jobs.length);
        //return jobs;
    }
	
	public boolean cancelJob(String queue, int jobID){
		return cups.cupsCancelJob(queue, jobID) == 1;
    }

	public cups_dest_s getPrinter(String queue){
    	cups_dest_s.ByReference[] dests = new cups_dest_s.ByReference[1];
    	dests[0] = new cups_dest_s.ByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, dests);
    	cups_dest_s ret = cups.cupsGetDest(queue, null, s, dests[0]);
    	return ret;
    }
	
	public cups_dest_s.ByReference[] listPrinters(){
    	cups_dest_s.ByReference[] dests = new cups_dest_s.ByReference[1];
    	dests[0] = new cups_dest_s.ByReference();
    	cups.cupsGetDests2(serv_conn_p, dests);
		return dests;
	}
    
    public int print(cups_dest_s printer, CupsPrintJob printJob) throws Exception{
    	Pointer m = new Memory(printJob.getJobName().length() + 1);
    	m.setString(0, printJob.getJobName());

    	int num_options = 0;
    	cups_option_s.ByReference[] options = new cups_option_s.ByReference[1];
    	options[0] = new cups_option_s.ByReference();
    	//cups_option_s options;
    	Map<String, String> attrs = printJob.getAttributes();
    	Object[] keys = attrs.keySet().toArray();
    	for (int i=0; i<keys.length; i++){
    		Log.d("CUPSCLIENT-PRINT", "key: "+keys[i]+", value: "+attrs.get(keys[i]));
    		try {
    			num_options = cups.cupsAddOption(keys[i].toString(), attrs.get(keys[i]), num_options, options);
    		} catch (Exception e){ e.printStackTrace(); }
    	}
    	
    	cups_option_s opts = options[0];
    	
    	int job_id = cups.cupsCreateJob(serv_conn_p, printer.name, m, num_options, opts);
    	if (job_id == 0) return 0;

    	//http_status_t
    	cups.cupsStartDocument(serv_conn_p, printer.name.getString(0), job_id, printJob.getJobName(), printJob.getMimeType(), 1);
    	
    	NativeSize length = new NativeSize(printJob.getDocumentLength());
    	
    	//http_status_t
    	Pointer buffer = new Memory(printJob.getDocumentLength());
    	buffer.write(0, printJob.getBytes(), 0, (int) printJob.getDocumentLength());
    	if (cups.cupsWriteRequestData(serv_conn_p, buffer, length) != http_status_e.HTTP_STATUS_CONTINUE)
    		return 0;
    	
    	//ipp_status_t
    	cups.cupsFinishDocument(serv_conn_p,  printer.name);
    	return job_id;
    }
    
    public int print(String queue, CupsPrintJob printJob) throws Exception {
    	return print(getPrinter(queue), printJob);
    }

    public String cupsGetPPD(String name){
    	Pointer p = cups.cupsGetPPD2(serv_conn_p, name);
    	String s = p.getString(0);
    	String ret = "";
    	try {
    		File file = new File(s);
			InputStream ppdIS = new FileInputStream(file);
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while ((len = ppdIS.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, len);
			}
			ret = byteBuffer.toString();
			byteBuffer.close();
			ppdIS.close();
			file.delete();
		} catch (FileNotFoundException e) {} catch (IOException e) {}
    	return ret;
    }
    
    public String getOption(cups_dest_s printer, String option){
    	Pointer p = cups.cupsGetOption("device-uri",printer.num_options, printer.options);
    	if (p==null) return null;
    	else return p.getString(0);
    }
    
    public String[] getAttribute(cups_dest_s printer, String attribute){
    	String[] retval = null;
    	PointerByReference request = cups.ippNewRequest(ipp_op_e.IPP_OP_GET_PRINTER_ATTRIBUTES);
    	cups.ippAddString(request, ipp_tag_e.IPP_TAG_OPERATION, ipp_tag_e.IPP_TAG_URI, "printer-uri", null, "/printers/"+printer.name.getString(0));
    	PointerByReference response = cups.cupsDoRequest(serv_conn_p, request, "/");

    	PointerByReference attr;
    	PointerByReference lang = null;
    	for (attr = cups.ippFirstAttribute(response); attr != null; attr = cups.ippNextAttribute(response)){
    		if (cups.ippGetName(attr) != null){
    			String[] cstring = new String[cups.ippGetCount(attr)];
    			for (int i=0; i<cups.ippGetCount(attr); i++)
    				cstring[i] = (cups.ippGetString(attr, i, lang)==null)?null:cups.ippGetString(attr, i, lang).getString(0);
    			if (cups.ippGetName(attr).getString(0).equals(attribute)) retval = cstring;
    		}
    	}
    	
    	return retval;
    }
}
