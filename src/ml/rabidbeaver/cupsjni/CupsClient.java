package ml.rabidbeaver.cupsjni;

import java.nio.IntBuffer;

import android.util.Log;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary;
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
	
	public void setUserPass(String userName, String password){
		//TODO: obviously, this needs to actually do something to actually set up authentication....
		this.userName=userName;
		this.password=password;
	}
	
	public boolean isPrinterAccessible(String queue){
		if (getPrinter(queue) == null) return false;
		return true;
	}
	
	public ByReference[] getJobs(String queue, int whichJobs, boolean myJobs){    
        ByReference[] jobs = null;
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
        //TODO return print(printer.getPrinterUrl(), printJob);

    	Pointer m = new Memory(printJob.getJobName().length() + 1);
    	m.setString(0, printJob.getJobName());
    	
    	//TODO: probably should be job options instead of printer options....
    	int job_id = cups.cupsCreateJob(serv_conn_p, printer.name, m, printer.num_options, printer.options);
    	if (job_id == 0) return 0;

    	//http_status_t
    	cups.cupsStartDocument(serv_conn_p, printer.name.getString(0), job_id, printJob.getJobName(), printJob.getMimeType(), 1);
    	String document = printJob.getDocumentString();
    	NativeSize length = new NativeSize(document.length());
    	
    	//http_status_t
    	//TODO: can actually break this into multiple runs of cupsWriteRequestData of sensible length, like 1024.
    	if (cups.cupsWriteRequestData(serv_conn_p, document, length) != http_status_e.HTTP_STATUS_CONTINUE)
    		return 0;
    	
    	//ipp_status_t
    	cups.cupsFinishDocument(serv_conn_p,  printer.name);
    	return job_id;
    }
    
    public int print(String queue, CupsPrintJob printJob) throws Exception {
    	return print(getPrinter(queue), printJob);
    }

    public Pointer cupsGetPPD(String name){
    	return cups.cupsGetPPD2(serv_conn_p, name);
    }
    
    public String getOption(cups_dest_s printer, String option){
    	Pointer p = cups.cupsGetOption("device-uri",printer.num_options, printer.options);
    	if (p==null) return null;
    	else return p.getString(0);
    }
    
    public String[] getAttribute(cups_dest_s printer, String attribute){
    	Log.d("CUPSCLIENT-GETATTRIBUTES",printer.name.getString(0));
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
