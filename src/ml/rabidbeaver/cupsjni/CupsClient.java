package ml.rabidbeaver.cupsjni;

import java.net.URL;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.ipp_status_e;
import ml.rabidbeaver.cupsjni.cups_job_s.ByReference;

/*
 * Every time a native header in this file is added or altered, it is necessary to enter CupsClientService/jni/include and run the following command:
 * javah -classpath ../../bin/classes -o shim.h ml.rabidbeaver.cupsjni.CupsClient
 * to regenerate the shim.h
 */

public class CupsClient {
	private String userName = "anonymous";
	private String password;
	private MlRabidbeaverCupsjniLibrary cups;
	private PointerByReference serv_conn;
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	public final int CUPS_WHICHJOBS_ACTIVE = cups.CUPS_WHICHJOBS_ACTIVE;
	
	private static final String listAttrs = 
			"device-uri printer-name printer-info printer-location printer-make-and-model printer-uri-supported";
	private static final String stdAttrs = 
			"device-uri printer-name requesting-user-name-allowed requesting-user-name-denied printer-info printer-location printer-make-and-model printer-uri-supported";
	private static final String extAttrs = "document-format-supported";
	
	// Load jni library
	//static {
	//	System.loadLibrary("cups_shim");
	//}
	
	// Constructors
	public CupsClient(String host, int port, String userName){
		this.userName=userName;
		serv_conn=cups.httpConnect(host, port);
	}
	public CupsClient(String host, int port){
		serv_conn=cups.httpConnect(host, port);
	}
	public CupsClient(){}
	
	public void setUserPass(String userName, String password){
		this.userName=userName;
		this.password=password;
	}
	
	public boolean isPrinterAccessible(String name, String queue){
		if (cups.cupsGetDestWithURI(name, queue) == null) return false;
		return true;
	}
	
	public cups_dest_s.ByReference[] listPrinters(){
		cups_dest_s.ByReference[] dests = null;
		cups.cupsGetDests2(serv_conn, dests);
		return dests;
    }
	

	public ByReference[] getJobs(String queue, int whichJobs, boolean myJobs){    
        ByReference[] jobs = null;
		cups.cupsGetJobs2(serv_conn, jobs, queue, myJobs?1:0, whichJobs);
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
    
    public cups_dest_s getPrinter(String queue, boolean extended){
        if (extended)
            return getPrinter(queue, stdAttrs + " " + extAttrs);
        else 
            return getPrinter(queue, stdAttrs);
    }
    
    private cups_dest_s getPrinter(String queue, String attrs){
        /* TODO URL printerUrl = new URL(url.toString() + queue);
        IppGetPrinterAttributesOperation op = new IppGetPrinterAttributesOperation();
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("requested-attributes", attrs);
        OperationResult result = op.getPrinterAttributes(printerUrl, userName, auth, map);
        String status = result.getHttpStatusResult();
        if (!(status.contains("200"))){
            throw new Exception(status);
        }
        for (AttributeGroup group : result.getIppResult().getAttributeGroupList()) {
            if (group.getTagName().equals("printer-attributes-tag")) {
                return setPrinter(url, group);
            }
            else if (group.getTagName().equals("unsupported-attributes-tag")) {
                throw new Exception(this.url + " is not a CUPS server");
            }
        }*/
        return null;
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
    	return cups.cupsGetPPD2(serv_conn, name);
    }
    public Pointer cupsGetPPD(String name){
    	return cups.cupsGetPPD(name);
    }
}
