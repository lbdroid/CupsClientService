package ml.rabidbeaver.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;

public class PrinterDiscovery{
	
	private Map<String, PrinterDiscoveryInfo> printerInfos;
	private List<PrinterDiscoveryListener> printerDiscoveryListeners;
	
	public PrinterDiscovery(){
		printerInfos = Collections.synchronizedMap(new HashMap<String, PrinterDiscoveryInfo>());
		printerDiscoveryListeners = new ArrayList<PrinterDiscoveryListener>();
	}
	
    public Map<String, PrinterDiscoveryInfo> addDiscoveryListener(PrinterDiscoveryListener listener){
		printerDiscoveryListeners.add(listener);
		return getPrinters();
	}
	
	public void removeDiscoveryListener(PrinterDiscoveryListener listener){
		printerDiscoveryListeners.remove(listener);
	}
	
	
	public Map<String, PrinterDiscoveryInfo>getPrinters(){
		return new HashMap<String, PrinterDiscoveryInfo>(printerInfos);
	}
	
	public PrinterDiscoveryInfo getPrinterInfo(String queue){
		
		return printerInfos.get(queue);
	}
	
	private void readStaticConfig(){
		
		PrintQueueConfHandler confdb = new PrintQueueConfHandler(CupsPrintApp.getContext());
		ArrayList<String> iniPrintersArray = confdb.getPrintQueueConfigs();
		
		synchronized(printerInfos){
			Iterator<String> it = printerInfos.keySet().iterator();
			while (it.hasNext()){
				String key = it.next();
				PrintQueueConfig test = confdb.getPrinter(key);
				if (test == null){
					PrinterDiscoveryInfo info = printerInfos.get(key);
					if (info != null){
						if (info.setRemoveStatic()){
							it.remove();
							notifyRemovePrinter(info);
						}
					}
				}
			}
		}
		
		for(String nickname : iniPrintersArray){
			PrinterDiscoveryInfo pdInfo = printerInfos.get(nickname);
			if (pdInfo == null){
				PrintQueueConfig config = confdb.getPrinter(nickname);
				if (config != null){
					PrinterDiscoveryInfo newInfo = new PrinterDiscoveryInfo(nickname,config.getPrintQueue());
					newInfo.setStatic();
					printerInfos.put(nickname, newInfo);
					notifyAddPrinter(newInfo);
				}
			}
			else {
				pdInfo.setStatic();
			}
		}
		confdb.close();
	}
	
	private void notifyAddPrinter(PrinterDiscoveryInfo info){
		Iterator<PrinterDiscoveryListener> it = printerDiscoveryListeners.iterator();
		while (it.hasNext()){
			PrinterDiscoveryListener listener = it.next();
			try{
				listener.onPrinterAdded(info);
			}catch (Exception e){
				it.remove();
				System.err.println(e.toString());
			}
		}
	}
	
	public void notifyRemovePrinter(PrinterDiscoveryInfo info){
		Iterator<PrinterDiscoveryListener> it = printerDiscoveryListeners.iterator();
		while (it.hasNext()){
			PrinterDiscoveryListener listener = it.next();
			try{
				listener.onPrinterRemoved(info);
			}catch (Exception e){
				it.remove();
				System.err.println(e.toString());
			}
		}
	}
	
	public void updateStaticConfig(){
		new Thread(new staticUpdater()).start();
	}

	public class staticUpdater implements Runnable{

		@Override
		public void run() {
			readStaticConfig();
		}
	}

}	
