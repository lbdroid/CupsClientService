package ml.rabidbeaver.cupsjni;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

/*
 * Every time a native header in this file is added or altered, it is necessary to enter CupsClientService/jni/include and run the following command:
 * javah -classpath ../../bin/classes -o shim.h ml.rabidbeaver.cupsjni.CupsClient
 * to regenerate the shim.h
 */

public class CupsClient {
	public String url = null;
	private String userName = "anonymous";
	private String password;
	
	public static final int USER_AllOWED = 0;
	public static final int USER_DENIED = 1;
	public static final int USER_NOT_ALLOWED = 2;
	
	private static final String listAttrs = 
			"device-uri printer-name printer-info printer-location printer-make-and-model printer-uri-supported";
	private static final String stdAttrs = 
			"device-uri printer-name requesting-user-name-allowed requesting-user-name-denied printer-info printer-location printer-make-and-model printer-uri-supported";
	private static final String extAttrs = "document-format-supported";
	
	// Load jni library
	static {
		System.loadLibrary("cups_shim");
	}
	
	// Constructors
	public CupsClient(String url, String userName){
		this.url=url;
		this.userName=userName;
	}
	public CupsClient(String url){
		this.url=url;
	}
	
	public void setUserPass(String userName, String password){
		this.userName=userName;
		this.password=password;
	}
	
	// Structures
	public class cups_option_t {
		cups_option_t(String name, String value){
			this.name=name;
			this.value=value;
		}
		String name;
		String value;
	}
	public class cups_dest_t {
		cups_dest_t(String name, String instance, boolean is_default, int num_options, cups_option_t[] options){
			this.name=name;
			this.instance=instance;
			this.is_default=is_default;
			this.num_options=num_options;
			this.options=options;
		}
		public String getOption(String name){
			for (int i=0; i<num_options; i++){
				if (options[i].name.equals(name)) return options[i].value;
			}
			return null;
		}
		public String name;
		String instance;
		boolean is_default;
		int num_options;
		cups_option_t[] options;
	}

	public boolean isPrinterAccessible(String name, String queue){
		if (cupsGetDestWithURI(name, queue) == null) return false;
		return true;
	}
	
	public native cups_dest_t cupsGetDestWithURI(String name, String queue);
	public native List<cups_dest_t> cupsGetDests2(String url);
	
	public List<cups_dest_t> listPrinters(){
		return cupsGetDests2(url);
    }
	
	public List<CupsPrintJobAttributes> getJobs(String queue, WhichJobsEnum whichJobs, boolean myJobs) throws IOException, Exception {    
        URL printerUrl = new URL(url.toString() + queue);
        //TODO: return new IppGetJobsOperation().getPrintJobs(printerUrl, auth, userName, whichJobs, myJobs);
        return null;
    }
	
	public boolean /*PrintRequestResult*/ cancelJob(int jobID) throws UnsupportedEncodingException, IOException{
        //TODO: return new IppCancelJobOperation().cancelJob(url, auth, userName, jobID);
		return true;
    }

    public boolean /*PrintRequestResult*/ holdJob(int jobID) throws UnsupportedEncodingException, IOException{
    	//TODO: return new IppHoldJobOperation().holdJob(url, auth, userName, jobID);
    	return true;
    }

    public boolean /*PrintRequestResult*/ releaseJob(int jobID) throws UnsupportedEncodingException, IOException{
    	//TODO: return new IppReleaseJobOperation().releaseJob(url, auth, userName, jobID);
    	return true;
    }
    
    public cups_dest_t getPrinter(String queue, boolean extended) throws UnsupportedEncodingException, IOException, Exception{
        if (extended)
            return getPrinter(queue, stdAttrs + " " + extAttrs);
        else 
            return getPrinter(queue, stdAttrs);
    }
    
    private cups_dest_t getPrinter(String queue, String attrs) throws UnsupportedEncodingException, IOException, Exception{
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
}
