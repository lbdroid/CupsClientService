package ml.rabidbeaver.cupsjni;

public class CupsClient {
	public String url = null;
	public String userName = "anonymous";
	
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
		String name;
		String instance;
		boolean is_default;
		int num_options;
		cups_option_t[] options;
	}
	//private static native cups_dest_t cupsGetDestWithURI(String name, String uri);
	//
	public boolean isPrinterAccessible(String name, String queue){
		if (cupsGetDestWithURI(name, queue) == null) return false;
		return true;
	}
	
	public native cups_dest_t cupsGetDestWithURI(String name, String queue);

}
