package ml.rabidbeaver.detect;

public class PrinterRec implements Comparable<PrinterRec>{
    
	private String nickname;
	private String protocol;
	private String host;
	private int    port;
	private String queue;
    
    public PrinterRec(String nickname, String protocol, String host,
            int port, String queue){
        this.nickname = nickname;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.queue = queue;
        }
    
    public String getNickname(){
        return nickname;
     }

    public String getProtocol(){
    	return protocol;
    }
    public String getHost(){
    	return host;
    }

    public int getPort(){
    	return port;
    }
    
    public String getQueue(){
    	return queue;
    }

    @Override
    public String toString(){
        return nickname + " (" + protocol + ")";
    }

	@Override
	public int compareTo(PrinterRec another) {
		return this.toString().compareTo(another.toString());
	}


}
