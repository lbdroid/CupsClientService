package ml.rabidbeaver.discovery;

public class PrinterDiscoveryInfo {
	
	private String nickname;
	private String queue;
	private int status;
	private boolean isStatic = false;
	private boolean isDynamic = false;
	

	public PrinterDiscoveryInfo(String nickname, String queue){
		this.nickname = nickname;
		this.queue = queue;
	}
	
	public String getNickname(){
		return nickname;
	}
	
	public String getQueue(){
		return queue;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public void setStatic(){
		isStatic = true;
	}
	
	public void setDynamic(){
		isDynamic = true;
	}
	
	public boolean setRemoveStatic(){
		isStatic = false;
		if (!isDynamic){
			return true;
		}
		return false;
	}
	
	public boolean setRemoveDynamic(){
		isDynamic = false;
		if (!isStatic){
			return true;
		}
		return false;
	}
	
}
