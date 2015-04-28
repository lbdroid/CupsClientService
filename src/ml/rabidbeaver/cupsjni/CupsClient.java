package ml.rabidbeaver.cupsjni;

public class CupsClient {
	public String url = null;
	public String userName = "anonymous";
	public CupsClient(String url, String userName){
		this.url=url;
		this.userName=userName;
	}
	public CupsClient(String url){
		this.url=url;
	}
	public boolean isPrinterAccessible(String queue){
		//TODO: this needs to ultimately call jni cupsGetDestWithURI(NULL, url)
		// if (cupsGetDestWithURI(NULL, url) == NULL) return false; else return true;
		return false;
	}
	
	static {
		System.loadLibrary("cups");
	}
}
