package ml.rabidbeaver.cupsjni;

import java.net.URL;
import java.nio.IntBuffer;

import android.util.Log;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.cups_device_cb_t;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.http_encryption_e;
import ml.rabidbeaver.cupsjni.MlRabidbeaverCupsjniLibrary.http_t;
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
	private PointerByReference serv_conn_p;
	private http_t serv_conn;
	private IntBuffer i_b = IntBuffer.allocate(1);
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	private MlRabidbeaverCupsjniLibrary cups = MlRabidbeaverCupsjniLibrary.INSTANCE;
	public final int CUPS_WHICHJOBS_ACTIVE = 0;//MlRabidbeaverCupsjniLibrary.CUPS_WHICHJOBS_ACTIVE;
	
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
		Log.d("CUPSCLIENT",cups==null?"NULL":"notnull");
		serv_conn_p = cups.httpConnect2(host, port, null, 0, http_encryption_e.HTTP_ENCRYPTION_IF_REQUESTED, 0, 2500, i_b);
	}
	public CupsClient(String host, int port){
		Log.d("CUPSCLIENT",cups==null?"NULL":"notnull");
		//serv_conn=(http_t) cups.httpConnect(host, port).toNative();
		serv_conn_p = cups.httpConnect(host, port);
		//serv_conn_p = cups.httpConnect2(host, port, null, 0, http_encryption_e.HTTP_ENCRYPTION_IF_REQUESTED, 0, 2500, i_b);
		//listPrinters();
		//Log.d("CUPSCLIENT","SERVCONN:"+serv_conn.toString());
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
		//int num = cups.cupsGetDests2(serv_conn_p, dests);
		//Log.d("CUPSCLIENT","listPrinters(), num:"+num);
		//for (int i=0; i<num; i++){
		//	Log.d("CUPSCLIENT","listprinters():"+dests[i].name);
		//}
		
		int s;
		DevCB a = new DevCB();
		s=cups.cupsGetDevices(serv_conn_p, 100, "0", "0", a, null);
		Log.d("CUPSCLIENT","listPrinters(), status:"+((s==ipp_status_e.IPP_STATUS_ERROR_INTERNAL)?"INTERNAL ERROR":"Not internal error"));
		return dests;
    }
	
	private class DevCB implements cups_device_cb_t {

		@Override
		public void apply(Pointer device_class, Pointer device_id,
				Pointer device_info, Pointer device_make_and_model,
				Pointer device_uri, Pointer device_location, Pointer user_data) {
			// TODO Auto-generated method stub
			Log.d("CUPSCLIENT","callback running");
			
		}
		
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
    
    public cups_dest_s getPrinter(String queue, boolean extended){
        if (extended)
            return getPrinter(queue, stdAttrs + " " + extAttrs);
        else 
            return getPrinter(queue, stdAttrs);
    }
    
    @SuppressWarnings("deprecation")
	private cups_dest_s getPrinter(String queue, String attrs){
    	cups_dest_s.ByReference[] cds = new cups_dest_s.ByReference[1];
    	cds[0] = new cups_dest_s.ByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, cds);
    	Log.d("CUPSCLIENT","num dests:"+s);
    	cups_dest_s ret = cups.cupsGetDest(queue, null, s, cds[0]);

    	return ret;
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
}
