package ml.rabidbeaver.cupsjni;

import java.net.URL;
import java.nio.IntBuffer;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.http_encryption_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.ipp_op_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.ipp_status_e;
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
    
    public ipp_status_e print(cups_dest_s printer, CupsPrintJob printJob) throws Exception{
        //TODO return print(printer.getPrinterUrl(), printJob);
    	return null;
    }
    
    public ipp_status_e print(String queue, CupsPrintJob printJob) throws Exception {
         //TODO URL printerUrl = new URL(url.toString() + queue);
         //return print(printerUrl, printJob);
    	return null;
    }
   
    private ipp_status_e print(URL printerUrl, CupsPrintJob printJob) throws Exception {
        //TODO IppPrintJobOperation command = new IppPrintJobOperation();
        //return command.print(printerUrl, userName, printJob);
    	return null;
    }
    
    public Pointer cupsGetPPD2(String name){
    	return MlRabidbeaverCupsjniLibrary.INSTANCE.cupsGetPPD2(serv_conn_p, name);
    }
    public Pointer cupsGetPPD(String name){
    	return MlRabidbeaverCupsjniLibrary.INSTANCE.cupsGetPPD(name);
    }
    
    public String getOption(cups_dest_s printer, String option){
    	Pointer p = cups.cupsGetOption("device-uri",printer.num_options, printer.options);
    	if (p==null) return null;
    	else return p.getString(0);
    }
    
    public String[] getAttribute(cups_dest_s printer, String attribute){
    	String[] retval = null;
    	PointerByReference request = cups.ippNewRequest(ipp_op_e.IPP_OP_GET_PRINTER_ATTRIBUTES);
    	cups.ippAddString(request, ipp_tag_e.IPP_TAG_OPERATION, ipp_tag_e.IPP_TAG_URI, "printer-uri", null, "ipp://127.0.0.1:631/printers/P1102w");
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
