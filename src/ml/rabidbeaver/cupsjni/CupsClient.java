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
import com.sun.jna.ptr.IntByReference;
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
		if (getPrinter(queue, false) == null) return false;
		return true;
	}
	

	public cups_job_s[] getJobs(String queue, int whichJobs, boolean myJobs){
		PointerByReference p = new PointerByReference();
		int num = cups.cupsGetJobs2(serv_conn_p, p, queue, myJobs?1:0, whichJobs);
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
		PointerByReference p = new PointerByReference();
    	int s = cups.cupsGetDests2(serv_conn_p, p);
		Pointer ptr = p.getValue();
		cups_dest_s cdest = new cups_dest_s(ptr);
		cdest.read();
		cups_dest_s[] dests = (cups_dest_s[]) cdest.toArray(s);
    	cups_dest_s ret = cups.cupsGetDest(queue, null, s, dests[0]);
    	
    	//cupsGetPPD(queue);
    	dumpPrinterAttrs(ret);
    	
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
    	return print(getPrinter(queue, false), printJob);
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
			while ((len = ppdIS.read(buffer)) != -1)
				byteBuffer.write(buffer, 0, len);
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
    
    public void dumpPrinterAttrs(cups_dest_s printer){
    	PointerByReference request = cups.ippNewRequest(ipp_op_e.IPP_OP_GET_PRINTER_ATTRIBUTES);
    	cups.ippAddString(request, ipp_tag_e.IPP_TAG_OPERATION, ipp_tag_e.IPP_TAG_URI, "printer-uri", null, "/printers/"+printer.name.getString(0));
    	PointerByReference response = cups.cupsDoRequest(serv_conn_p, request, "/");

    	PointerByReference attr;

    	for (attr = cups.ippFirstAttribute(response); attr != null; attr = cups.ippNextAttribute(response)){
    		dumpAttrs(attr);
    	}
    }
    
    private void dumpAttrs(PointerByReference attr){
    	PointerByReference lang = null;
    	int type = cups.ippGetValueTag(attr);
		if (cups.ippGetName(attr) != null){
			String[] cstring = new String[cups.ippGetCount(attr)];

			for (int i=0; i<cups.ippGetCount(attr); i++){
				switch (type){
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
				case ipp_tag_e.IPP_TAG_RANGE:
					IntByReference upper = new IntByReference();
					int lower = cups.ippGetRange(attr, i, upper);
					cstring[i] = Integer.toString(lower)+"-"+Integer.toString(upper.getValue());
					break;

				case ipp_tag_e.IPP_TAG_TEXT:
				case ipp_tag_e.IPP_TAG_NAME:
				case ipp_tag_e.IPP_TAG_KEYWORD:
				case ipp_tag_e.IPP_TAG_URI:
				case ipp_tag_e.IPP_TAG_CHARSET:
				case ipp_tag_e.IPP_TAG_LANGUAGE:
				case ipp_tag_e.IPP_TAG_MIMETYPE:
					cstring[i] = cups.ippGetString(attr, i, lang).getString(0);
					break;

				default:
					break;
				}
			}
			for (int j=0; j<cstring.length; j++){
				String attrname;
				if (cups.ippGetName(attr) ==null) attrname="nullname";
				else attrname = cups.ippGetName(attr).getString(0);
				if (cstring[j] != null) Log.d("CUPSCLIENT-DUMPPRINTERATTRS",attrname+" : "+ cstring[j]);
			}
		}
    }
}
