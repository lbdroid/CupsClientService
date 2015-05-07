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
	

	public cups_job_s[] getJobs(String queue, int whichJobs, boolean myJobs){
		PointerByReference p = new PointerByReference();
		int num = cups.cupsGetJobs2(serv_conn_p, p, queue, myJobs?1:0, MlRabidbeaverJnaLibrary.CUPS_WHICHJOBS_ALL);//whichJobs);
		Pointer ptr = p.getValue();
		cups_job_s cjob = new cups_job_s(ptr);
		cjob.read();
		cups_job_s[] jobarr = (cups_job_s[]) cjob.toArray(num);
		
		for (int i=0; i<num; i++){
			Log.d("CUPSCLIENT-GETJOBS",jobarr[i].title.getString(0));
		}
		
		return jobarr;
    }
	
	public boolean cancelJob(String queue, int jobID){
		return cups.cupsCancelJob(queue, jobID) == 1;
    }

	public cups_dest_s getPrinter(String queue){
		PointerByReference p = new PointerByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, p);
    	
		Pointer ptr = p.getValue();
		cups_dest_s cdest = new cups_dest_s(ptr);
		cdest.read();
		cups_dest_s[] dests = (cups_dest_s[]) cdest.toArray(s);
    	
    	cups_dest_s ret = cups.cupsGetDest(queue, null, s, dests[0]);
    	return ret;
    }
	
	public cups_dest_s[] listPrinters(){
		PointerByReference p = new PointerByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, p);
    	
		Pointer ptr = p.getValue();
		cups_dest_s cdest = new cups_dest_s(ptr);
		cdest.read();
		cups_dest_s[] dests = (cups_dest_s[]) cdest.toArray(s);
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
