package ml.rabidbeaver.cupsjni;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.cups_password_cb_t;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.http_status_e;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.ipp_op_e;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.ipp_tag_e;
import ml.rabidbeaver.jna.cups_dest_s;
import ml.rabidbeaver.jna.cups_job_s;
import ml.rabidbeaver.jna.cups_option_s;

public class CupsClient {
	private String userName = "anonymous";
	private String password;
	private PointerByReference serv_conn_p = null;
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	private MlRabidbeaverJnaLibrary cups = MlRabidbeaverJnaLibrary.INSTANCE;
	
	private ServiceConnection mConnection = null;
	private Messenger mService = null;
    private String Uuid;
    private int tunnelPort;
    private Context ctx;
    private boolean cupsready = false;
    private boolean tunnelconn = false;
	
	// Constructors
	public CupsClient(String host, int port, String tunnelUuid, int tunnelPort, boolean tunnelFallback, Context ctx){
		this.ctx=ctx;
		if (tunnelUuid != null && tunnelUuid.length() > 1){
			if ((tunnelFallback && (serv_conn_p = cups.httpConnect(host, port)) == null) || !tunnelFallback){
				// try the tunnel
				tunnelconn = true;
				this.Uuid=tunnelUuid;
				this.tunnelPort=tunnelPort;
				loadServiceConnection();
				Intent intent = new Intent();
				intent.setComponent(new ComponentName("ml.rabidbeaver.ssh", "ml.rabidbeaver.ssh.TunnelService"));
				ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			}
		} else serv_conn_p = cups.httpConnect(host, port);
		if (serv_conn_p != null) cupsready = true;
	}
	
	public void cleanup(){
		if (mConnection != null){
			Message msg = Message.obtain(null, 3 /*MSG_DROP_ALL_TUNNELS*/, android.os.Process.myPid(), 0);
        	try {
				mService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			ctx.unbindService(mConnection);
		}
	}
	
	public void loadServiceConnection(){
		mConnection = new ServiceConnection() {
	        public void onServiceConnected(ComponentName className, IBinder service) {
	        	Log.d("CUPSCLIENT","setting up service");
	            mService = new Messenger(service);
	            Bundle bundle = new Bundle();
	            bundle.putString("uuid", Uuid);
	            Message msg = Message.obtain(null, 1 /*MSG_HOLDOPEN_TUNNEL*/, android.os.Process.myPid(), 0, bundle);
            	try {
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
            	int i=0;
            	while (serv_conn_p == null){
            		serv_conn_p = cups.httpConnect("localhost", tunnelPort);
            		i++;
            		if (serv_conn_p != null || i>20) break;
            		try { Thread.sleep(1000); } catch (InterruptedException e) {}
            	}
            	if (serv_conn_p != null) cupsready = true;
            	else tunnelconn = false; // don't wait on connection that will never happen
	        }

	        public void onServiceDisconnected(ComponentName className) {
	            mService = null;
	        }
	    };
	}
	
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
		if (getPrinter(queue, false) == null) return false;
		return true;
	}
	
	private void waitForTunnel(){
		int counter = 0;
		while (tunnelconn && !cupsready){
			try { Thread.sleep(500); } catch (InterruptedException e) {}
			counter++;
			if (counter > 40) break;
		}
	}

	public cups_job_s[] getJobs(String queue, int whichJobs, boolean myJobs){
		waitForTunnel();
		if (serv_conn_p == null) return null;
		PointerByReference p = new PointerByReference();
		int num = cups.cupsGetJobs2(serv_conn_p, p, queue, myJobs?1:0, whichJobs);
		if (num == 0) return null;
		Pointer ptr = p.getValue();
		cups_job_s cjob = new cups_job_s(ptr);
		cjob.read();
		cups_job_s[] jobarr = (cups_job_s[]) cjob.toArray(num);
		return jobarr;
    }
	
	public boolean cancelJob(String queue, int jobID){
		return cups.cupsCancelJob(queue, jobID) == 1;
    }

	public cups_dest_s getPrinter(String queue, boolean extended){
		waitForTunnel();
		if (serv_conn_p == null) return null;
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
		waitForTunnel();
		if (serv_conn_p == null) return null;
		PointerByReference p = new PointerByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, p);
		Pointer ptr = p.getValue();
		cups_dest_s cdest = new cups_dest_s(ptr);
		cdest.read();
		cups_dest_s[] dests = (cups_dest_s[]) cdest.toArray(s);
		
		return dests;
	}
    
    public int print(cups_dest_s printer, CupsPrintJob printJob) throws Exception{
    	waitForTunnel();
    	if (serv_conn_p == null) return -1;
    	Pointer m = new Memory(printJob.getJobName().length() + 1);
    	m.setString(0, printJob.getJobName());

    	int num_options = 0;
    	PointerByReference options = new PointerByReference();
    	List<JobOptions> attrs = printJob.getAttributes();
    	for (int i=0; i<attrs.size(); i++){
    		JobOptions j = attrs.get(i);
    		try {
    			num_options = cups.cupsAddOption(j.name, j.value, num_options, options);
    		} catch (Exception e){ e.printStackTrace(); }
    	}
    	
    	cups_option_s opts = new cups_option_s(options.getValue());
    	opts.read();

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
    	return print(getPrinter(queue, false), printJob);
    }
    
    public List<JobOptions> getPrinterOptions(cups_dest_s printer){
    	List<JobOptions> printerOptions = new ArrayList<JobOptions>();
    	waitForTunnel();
    	if (serv_conn_p == null) return printerOptions;
    	PointerByReference request = cups.ippNewRequest(ipp_op_e.IPP_OP_GET_PRINTER_ATTRIBUTES);
    	cups.ippAddString(request, ipp_tag_e.IPP_TAG_OPERATION, ipp_tag_e.IPP_TAG_URI, "printer-uri", null, "/printers/"+printer.name.getString(0));
    	PointerByReference response = cups.cupsDoRequest(serv_conn_p, request, "/");

    	PointerByReference attr;
    	PointerByReference lang = null;
    	
    	for (attr = cups.ippFirstAttribute(response); attr != null; attr = cups.ippNextAttribute(response)){
    		int type = cups.ippGetValueTag(attr);
    		if (cups.ippGetName(attr) != null){
    			String[] cstring = new String[cups.ippGetCount(attr)];

    			for (int i=0; i<cups.ippGetCount(attr); i++){
    				switch (type){
    				case ipp_tag_e.IPP_TAG_TEXT:
    				case ipp_tag_e.IPP_TAG_NAME:
    				case ipp_tag_e.IPP_TAG_KEYWORD:
    				case ipp_tag_e.IPP_TAG_URI:
    				case ipp_tag_e.IPP_TAG_CHARSET:
    				case ipp_tag_e.IPP_TAG_LANGUAGE:
    				case ipp_tag_e.IPP_TAG_MIMETYPE:
    					cstring[i] = cups.ippGetString(attr, i, lang).getString(0);
    					break;
    				case ipp_tag_e.IPP_TAG_RANGE:
    					IntByReference upper = new IntByReference();
    					int lower = cups.ippGetRange(attr, i, upper);
    					cstring[i] = Integer.toString(lower)+"-"+Integer.toString(upper.getValue());
    					break;
    				case ipp_tag_e.IPP_TAG_INTEGER:
    				case ipp_tag_e.IPP_TAG_ENUM:
    					cstring[i] = Integer.toString(cups.ippGetInteger(attr, i));
    					break;
    				case ipp_tag_e.IPP_TAG_BOOLEAN:
    					cstring[i] = Boolean.toString(cups.ippGetBoolean(attr, i)==1);
    					break;
    				case ipp_tag_e.IPP_TAG_DATE:
    					cstring[i] = null;
    					break;
    				case ipp_tag_e.IPP_TAG_RESOLUTION:
    					//NOTE: our output string is NOT a valid way to set resolution to create a job.
    					IntByReference vres = new IntByReference();
    					IntByReference units = new IntByReference();
    					int hres = cups.ippGetResolution(attr, i, vres, units);
    					cstring[i] = Integer.toString(hres)+"x"+Integer.toString(vres.getValue());
    					break;

    				default:
    					break;
    				}
    			}
    			for (int j=0; j<cstring.length; j++){
    				String attrname;
    				if (cups.ippGetName(attr) ==null) attrname="nullname";
    				else attrname = cups.ippGetName(attr).getString(0);
    				if (cstring[j] != null)
    					printerOptions.add(new JobOptions(attrname, cstring[j]));
    			}
    		}
    	}
		return printerOptions;
    }
}
