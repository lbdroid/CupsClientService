package ml.rabidbeaver.detect;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.jmdns.ServiceInfo;
import com.jmdns.impl.DNSIncoming;
import com.jmdns.impl.DNSRecord;

import ml.rabidbeaver.cupsjni.CupsClient;

public class MdnsServices{
	
	ProgressUpdater updater;
	
	public MdnsServices(ProgressUpdater updater){
		this.updater = updater;
	}
    static final byte[] HEADER = {0,0,0,0,0,1,0,0,0,0,0,0};
    static final byte[] FOOTER =  {0,0,12,0,1};
    public static final String IPP_SERVICE = "_ipp._tcp.local.";
    public static final String IPPS_SERVICE = "_ipps._tcp.local.";
    static final int TIMEOUT = 1000;
    public static final int MAX_PASSES = 50;
    boolean error = false;
    
   
    private byte[] makeQuestion(String data){
        char lastChar = data.charAt(data.length()-1);
        if (lastChar == '.'){
            data = data.substring(0, data.length()-1);
        }
        ByteBuffer bytes = ByteBuffer.allocateDirect(data.length()+1);
        String[] parts = data.split("\\.");
        for (String part: parts){
            bytes.put((byte)part.length());
            bytes.put(part.getBytes());
        }
        bytes.flip();
        byte [] ret = new byte[bytes.capacity()];
        bytes.get(ret);
        return ret;
        
    }
    
    private byte[] makeMessage(String data){
        byte[] question = makeQuestion(data);
        byte[] message = new byte[HEADER.length + 
                question.length + FOOTER.length];
        int pos = 0;
        System.arraycopy(HEADER, 0, message, pos, HEADER.length);
        pos = pos + HEADER.length;
        System.arraycopy(question, 0, message, pos, question.length);
        pos = pos + question.length;
        System.arraycopy(FOOTER, 0, message, pos, FOOTER.length);
        return message;
    }
        
    private void process(Map <String, PrinterRec>list,
            DatagramPacket packet, String service){
        String protocol = "http";
        if (service.equals(IPPS_SERVICE)){
            protocol = "https";
        }
        try {
            DNSIncoming in = new DNSIncoming(packet);
            if (in.getNumberOfAnswers() < 1)
                return;
            Collection<? extends DNSRecord>answers = in.getAllAnswers();
            Iterator<? extends DNSRecord>iterator = answers.iterator();
            ServiceInfo info;
            
            Map<String, String>hosts = new HashMap<String, String>();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                if (record instanceof DNSRecord.Address){
                    info  = record.getServiceInfo();
                    String ip = info.getHostAddresses()[0];
                    hosts.put(info.getName() + "." + info.getDomain() + ".", ip);
                    iterator.remove();
                }
            }
            Map<String, String[]> services = new HashMap<String, String[]>();
            iterator = answers.iterator();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                if (record instanceof DNSRecord.Service){
                    info = record.getServiceInfo();
                    services.put(info.getKey(), new String[]{
                        hosts.get(info.getServer()),
                        String.valueOf(info.getPort())});
                    iterator.remove();
                }
            }
            
            iterator = answers.iterator();
            while (iterator.hasNext()){
                DNSRecord record = iterator.next();
                info = record.getServiceInfo();
                if (!(record instanceof DNSRecord.Text)){
                    continue;
                }
                if (!(info.getType().equals(service))){
                    continue;
                }
                String rp= info.getPropertyString("rp");
                if (rp==null){
                    continue;
                }
                String[] rps = rp.split("/");
                try{
                    rp = rps[rps.length-1];
                }catch (Exception e){
                    rp = "";
                }
                //System.out.println(info.getQualifiedName());
                String key = info.getKey();
                
                list.put(key, new PrinterRec(
                        info.getName(),
                        protocol,
                        services.get(key)[0],
                        Integer.parseInt(services.get(key)[1]),
                        rp
                        ));
                //System.out.println(info.toString());
            }
            
            //System.out.println();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
    
    private Map<String, PrinterRec> getPrinters(String service, int stage){
         Map<String, PrinterRec> printers = new HashMap<String,PrinterRec>();
         try{
            MulticastSocket s;
            InetAddress group;
            group = InetAddress.getByName("224.0.0.251");
            s = new MulticastSocket(5353);
            s.setSoTimeout(TIMEOUT);
            s.joinGroup(group);
            byte[] packet = makeMessage(service);
            DatagramPacket hi = new DatagramPacket(packet, packet.length,
                         group, 5353);
            s.send(hi);
            byte[] buf = new byte[65535];
            DatagramPacket recv = new DatagramPacket(buf, buf.length);
            error = false;
            int passes = 1;
            while (!error){
                try{
                    s.receive(recv);
                    process(printers, recv, service);
                    recv.setLength(buf.length);
                    passes ++;
                    if (passes > MAX_PASSES){
                    	updater.DoUpdate(passes + stage);
                        error = true;
                    }
            }
                catch (Exception e){
                    error = true;
                }
            }
            updater.DoUpdate(MAX_PASSES + stage);
            //System.out.println(passes);
            s.leaveGroup(group);
            s.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        return printers;
    }
    
    public PrinterResult scan(Context ctx){
        ArrayList<PrinterRec> httpRecs = new ArrayList<PrinterRec>();
        ArrayList<PrinterRec> httpsRecs = new ArrayList<PrinterRec>();
        httpRecs.addAll(getPrinters(MdnsServices.IPP_SERVICE, 0).values());
        httpsRecs.addAll(getPrinters(MdnsServices.IPPS_SERVICE, 50).values());
        
        PrinterResult result = new PrinterResult();
        String urlStr;
        Map<String, Boolean> testMap = new HashMap<String, Boolean>();
        Iterator<PrinterRec> it = httpsRecs.iterator();
        while(it.hasNext()){
        	PrinterRec rec = it.next();
     		urlStr = rec.getProtocol() + "://" + rec.getHost() + ":" + rec.getPort(); 
        	if (testMap.containsKey(urlStr)){
        		if (!testMap.get(urlStr))
        			it.remove();
        	}
        	else {
        		try {
        			CupsClient client = new CupsClient(urlStr, "");
        			if (client.isPrinterAccessible("")) // if this client is not an accessible printer on ssl, it will return false
        				testMap.put(urlStr, true);
        			else throw new Exception("Printer Not Accessible");
        		}
        		catch (Exception e){
        			testMap.put(urlStr, false);
        			it.remove();
        		}
        	}
        }
        new Merger().merge(httpRecs, httpsRecs);
        result.printerRecs = httpsRecs;
        return result;
    }
    
    
    public void stop(){
        error = true;

	}
    
}
