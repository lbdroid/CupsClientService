package ml.rabidbeaver.cupsprint;

import java.util.List;

import ml.rabidbeaver.cupsjni.JobOptions;

public class PrintQueueConfig {

	String nickname;
	String protocol;
	public String host;
	public String port;
	public String queue;
	String userName;
	String password;
	String tunnel;
	String tunneluuid;
	String tunnelport;
	boolean tunnelfallback;
	boolean isDefault;
	List<JobOptions> printerAttributes;
	
	
	public PrintQueueConfig(String nickname, String protocol, String host, String port, String queue, String tunnel, String tunneluuid, String tunnelport, boolean tunnelfallback){
		this.nickname = nickname;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.queue = queue;
		this.tunnel = tunnel;
		this.tunneluuid = tunneluuid;
		this.tunnelport = tunnelport;
		this.tunnelfallback = tunnelfallback;
	}
	
	public String getNickname(){
		return nickname;
	}
	
	public String getClient(){
		return protocol + "://" + host + ":" + port;
	}
	
	public String getQueuePath(){
		return "/printers/" + queue;
	}
	
	public String getPrintQueue(){
		return protocol + "://" + host + ":" + port + "/printers/" + queue;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getTunnelUuid(){
		return tunneluuid;
	}
	
	public int getTunnelPort(){
		return Integer.parseInt(tunnelport);
	}
	
	public boolean getTunnelFallback(){
		return tunnelfallback;
	}
		
	public List<JobOptions> getPrinterAttributes(){
		return printerAttributes;
	}
}
