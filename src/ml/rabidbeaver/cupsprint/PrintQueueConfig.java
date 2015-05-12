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
	String orientation;
	boolean imageFitToPage;
	boolean noOptions;
	boolean isDefault;
	String extensions;
	String resolution;
	List<JobOptions> printerAttributes;
	
	
	public PrintQueueConfig(String nickname, String protocol, String host, String port, String queue){
		this.nickname = nickname;
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.queue = queue;
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
	
	public String getResolution(){
		return resolution;
	}
}
