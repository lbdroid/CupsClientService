package ml.rabidbeaver.cupsjni;

import java.net.URL;

public class CupsClient {
	public URL url = null;
	public String userName = "anonymous";
	public CupsClient(URL url, String userName){
		this.url=url;
		this.userName=userName;
	}
	public CupsClient(URL url){
		this.url=url;
	}
	
	static {
		System.loadLibrary("cups");
	}
}
