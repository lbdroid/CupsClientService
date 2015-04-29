package ml.rabidbeaver.cupsjni;

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
}
