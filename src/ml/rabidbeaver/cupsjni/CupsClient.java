package ml.rabidbeaver.cupsjni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

import android.util.Log;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.cups_password_cb_t;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.http_encryption_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.http_status_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.ipp_op_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.ipp_tag_e;
import ml.rabidbeaver.cupsjni.cups_job_s.ByReference;

public class CupsClient {
	private String userName = "anonymous";
	private String password;
	private PointerByReference serv_conn_p;
	private IntBuffer i_b = IntBuffer.allocate(1);
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	private MlRabidbeaverCupsjniLibrary cups = MlRabidbeaverCupsjniLibrary.INSTANCE;
	public final int CUPS_WHICHJOBS_ACTIVE = MlRabidbeaverCupsjniLibrary.CUPS_WHICHJOBS_ACTIVE;
	
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
	
	public ByReference[] getJobs(String queue, int whichJobs, boolean myJobs){    
		cups_job_s.ByReference[] jobs = new cups_job_s.ByReference[1];
        cups.cupsGetJobs2(serv_conn_p, jobs, queue, myJobs?1:0, whichJobs);
        return jobs;
    }
	
	public boolean /*PrintRequestResult*/ cancelJob(int jobID){
        //TODO: return new IppCancelJobOperation().cancelJob(url, auth, userName, jobID);
		return true;
    }

    public boolean /*PrintRequestResult*/ holdJob(int jobID){
    	//TODO: return new IppHoldJobOperation().holdJob(url, auth, userName, jobID);
    	return true;
    }

    public boolean /*PrintRequestResult*/ releaseJob(int jobID){
    	//TODO: return new IppReleaseJobOperation().releaseJob(url, auth, userName, jobID);
    	return true;
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
    	
    	//TODO: probably should be job options instead of printer options....
    	int job_id = cups.cupsCreateJob(serv_conn_p, printer.name, m, printer.num_options, printer.options);
    	if (job_id == 0) return 0;

    	//http_status_t
    	cups.cupsStartDocument(serv_conn_p, printer.name.getString(0), job_id, printJob.getJobName(), printJob.getMimeType(), 1);
    	
    	NativeSize length = new NativeSize(printJob.getDocumentLength());
    	
    	//http_status_t
    	//TODO: can actually break this into multiple runs of cupsWriteRequestData of sensible length, like 1024.
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
    	//TODO: This does not appear to be reading the entire file into the String....
    	Log.d("CUPSCLIENT-CUPSGETPPD-1",name);
    	Pointer p = cups.cupsGetPPD2(serv_conn_p, name);
    	Log.d("CUPSCLIENT-CUPSGETPPD-2","ppd==null?"+p==null?"NULL":"notnull");
    	String s = p.getString(0);
    	Log.d("CUPSCLIENT-CUPSGETPPD-3",s);
    	String ret = "";
    	try {
			InputStream ppdIS = new FileInputStream(new File(s));
			InputStreamReader ppdReader = new InputStreamReader(ppdIS);
			BufferedReader bufferedReader = new BufferedReader(ppdReader);
            String receiveString = "";
            char[] buffer = new char[1024];
            while (bufferedReader.read(buffer) >= 0){
            	receiveString += new String(buffer);
            }
            ppdReader.close();
            ret = receiveString;
		} catch (FileNotFoundException e) {} catch (IOException e) {}
    	Log.d("CUPSCLIENT-CUPSGETPPD-4",ret);
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
