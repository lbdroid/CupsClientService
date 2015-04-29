package ml.rabidbeaver.detect;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;


//import org.cups4j.CupsClient;
//import org.cups4j.CupsPrinter;
//import org.cups4j.operations.AuthInfo;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsClient.cups_dest_t;
import android.content.Context;

public class IPScanner implements Runnable{
    String ipbase;
    int port = 631;
    String username;
    String password;
    Context ctx;
    
    public IPScanner(Context ctx, String ipbase, int port, String username, String password){
        this.ipbase = ipbase;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ctx=ctx;
    }
    
    @Override
    public void run() {
        IPTester.portTesters.incrementAndGet();
        Socket s = null;
        try {
            int ipnum;
            String ip = "";
            ipnum=IPTester.portIps.take();
            while (ipnum!= -1){
                try {
                    int hi = (ipnum & 0xFF00) >> 8;
                    int lo = ipnum & 0xFF;
                    ip = ipbase + "." + Integer.toString(hi) + "." + Integer.toString(lo);
                    s = new Socket();
                    s.connect(new InetSocketAddress(ip, port),IPTester.TIMEOUT);
                    s.close();
                    //System.out.println(ip + " open");
                    try {
                    	CupsClient cupsClient = new CupsClient("http://" + ip + ":" + port, "");
                    	if (!(password.equals(""))){
                    		cupsClient.setUserPass(username, password);
                    	}
                    	List<cups_dest_t> pList = cupsClient.listPrinters();
                    	for (cups_dest_t p: pList){
                    		PrinterRec rec = new PrinterRec(
                                p.getOption("printer-info"),
                                "http",
                                ip,
                                port,
                                p.name
                                );
                    		IPTester.httpResults.printerRecs.add(rec);
                    		//System.out.println(p.getName());
                    	}
                    }catch (Exception e){}
                    try {
                    	CupsClient cupsClient = new CupsClient("https://" + ip + ":" + port, "");
                    	if (!(password.equals(""))){
                    		cupsClient.setUserPass(username, password);
                    	}
                    	List<cups_dest_t> pList = cupsClient.listPrinters();
                    	for (cups_dest_t p: pList){
                    		PrinterRec rec = new PrinterRec(
                    			p.getOption("printer-info"),
                                "https",
                                ip,
                                port,
                                p.name
                                );
                    		IPTester.httpsResults.printerRecs.add(rec);
                        //System.out.println(p.getName());
                    	}
                    }catch (Exception e){
                    	System.out.println(e.toString());
                    }
                }
                catch (Exception e){}
                IPTester.tested.incrementAndGet();
                ipnum=IPTester.portIps.take();
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
         }
        finally {
            if (s != null){
                if (!s.isClosed()){
                    try{
                        s.close();
                    }
                    catch(Exception e){}
                }
            }              
        IPTester.portTesters.decrementAndGet();
        }
    }
}