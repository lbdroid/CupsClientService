package ml.rabidbeaver.printservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.discovery.PrinterDiscoveryInfo;
import ml.rabidbeaver.discovery.PrinterDiscoveryListener;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.widget.Toast;

public class CupsPrinterDiscoverySession extends PrinterDiscoverySession implements PrinterDiscoveryListener {

	private CupsPrintService printService;
	
	public CupsPrinterDiscoverySession(CupsPrintService printService){
		this.printService = printService;
	}
	
	@Override
	public void onDestroy(){
	}
	
	@Override
	public void onStartPrinterDiscovery(List<PrinterId> arg0) {
		Map<String, PrinterDiscoveryInfo> printerMap = CupsPrintApp.getPrinterDiscovery().addDiscoveryListener(this);
		Iterator<String> it = printerMap.keySet().iterator();
		List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
		while (it.hasNext()){
			PrinterDiscoveryInfo info = printerMap.get(it.next());
			PrinterInfo printerInfo = createPrinterInfo(info);
			if (printerInfo != null){
				printers.add(printerInfo);
			}
		}
		addPrinters(printers);
		ArrayList<PrinterId>printerIds = new ArrayList<PrinterId>();
		for (PrinterInfo printerInfo : this.getPrinters()){
			PrinterDiscoveryInfo info = printerMap.get(printerInfo.getName());
			if (info == null) printerIds.add(printerInfo.getId());
		}
		this.removePrinters(printerIds);
	 }

	@Override
	public void onStopPrinterDiscovery() {
		CupsPrintApp.getPrinterDiscovery().removeDiscoveryListener(this);
	}
	
	@Override
	public void onStartPrinterStateTracking(PrinterId printerId) {
		String nickName = printerId.getLocalId();

		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(CupsPrintApp.getContext());
		PrintQueueConfig config = dbconf.getPrinter(nickName);
		dbconf.close();
		if (config != null){
			if (!(config.getPassword().equals(""))){
				//TODO auth = new AuthInfo(CupsPrintApp.getContext(), config.getUserName(), config.getPassword());
			}
			/* TODO READ options from server HERE:
			GetServicePpdTask task = new GetServicePpdTask(config, md5);
			task.setPpdTaskListener(this);
			task.get(true, Thread.NORM_PRIORITY);*/
			setPrinterCapabilities(nickName);
		}
	
	}


	@Override
	public void onStopPrinterStateTracking(PrinterId arg0) {}

	@Override
	public void onValidatePrinters(List<PrinterId> arg0) {}

	@Override
	public void onPrinterAdded(final PrinterDiscoveryInfo info) {
		Handler handler = new Handler(CupsPrintApp.getContext().getMainLooper());
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				onPrinterAddedMainThread(info);
			}
		};
		handler.post(runnable);
	}
	
	public void onPrinterAddedMainThread(PrinterDiscoveryInfo info){
		List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
		PrinterInfo printerInfo = createPrinterInfo(info);
		if (printerInfo != null){
			printers.add(printerInfo);
			this.addPrinters(printers);
		}
	}

	@Override
	public void onPrinterRemoved(final PrinterDiscoveryInfo info) {
		Handler handler = new Handler(CupsPrintApp.getContext().getMainLooper());
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				onPrinterRemovedMainThread(info);
			}
		};
		handler.post(runnable);
	
	}
	
	private void onPrinterRemovedMainThread(PrinterDiscoveryInfo info){
		List<PrinterId> ids = new ArrayList<PrinterId>();
		PrinterId id = printService.generatePrinterId(info.getNickname());
		ids.add(id);
		this.removePrinters(ids);
	}
	
	private PrinterInfo createPrinterInfo(PrinterDiscoveryInfo info){
		PrinterId id = printService.generatePrinterId(info.getNickname());
		PrinterInfo.Builder builder = new PrinterInfo.Builder(id, info.getNickname(), PrinterInfo.STATUS_IDLE);
		try{
			return builder.build();
		}catch (Exception e){
			System.err.println(e.toString());
			return null;
		}
	}
	
	/* TODO
	 *   In this function, we actually TELL Android print frameworks what our printer is capable of.
	 *   I.e., define its specifications.
	 *   
	 *   We need to read the PrintQueueConfig and take applicable printer attributes and translate them
	 *   into a PrinterCapabilitiesInfo, and assign that to the printer.
	 */
	private void setPrinterCapabilities(String nickname){
		
		PrinterId id = printService.generatePrinterId(nickname);
		PrinterInfo.Builder infoBuilder = new PrinterInfo.Builder(id, nickname, PrinterInfo.STATUS_IDLE);
		PrinterCapabilitiesInfo.Builder capBuilder = new PrinterCapabilitiesInfo.Builder(id);
		
		/* TODO
		 *   Load dimensions as following:
		 */
		capBuilder.addMediaSize(new PrintAttributes.MediaSize("ISO_A4", "ISO_A4", 210, 297), true);
		//capBuilder.addMediaSize(MediaSize.ISO_A4, true);
		//  NOTE: second parameter flags the mediasize as "default".

		//PrintAttributes.MediaSize builtIn = PrintAttributes.MediaSize.ISO_A4;
		//PrintAttributes.MediaSize custom = new PrintAttributes.MediaSize("Letter", "Letter", 612, 792);
		//String s = builtIn.getLabel(CupsPrintApp.getContext().getPackageManager());
		
		/* TODO
		 *   Add printer resolutions as follows:
		 */
		capBuilder.addResolution(new PrintAttributes.Resolution("4x4", "5x5", 300, 300), true);
		capBuilder.addResolution(new PrintAttributes.Resolution("6x4", "6x5", 600, 600), false);
		capBuilder.addResolution(new PrintAttributes.Resolution("7x4", "7x5", 1200, 1200), false);

		/* TODO
		 *   Set color mode as per printer capabilities, probably should default to monochrome.
		 */
		capBuilder.setColorModes(PrintAttributes.COLOR_MODE_COLOR + PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_COLOR);
		
		capBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
		PrinterCapabilitiesInfo caps = null;
		PrinterInfo printInfo = null;
		try {
			caps = capBuilder.build();
			infoBuilder.setCapabilities(caps);
			printInfo = infoBuilder.build();
		}
		catch (Exception e){
			Toast.makeText(this.printService, e.toString(), Toast.LENGTH_LONG).show();
 			System.err.println(e.toString());
			return;
		}
		List<PrinterInfo> infos = new ArrayList<PrinterInfo>();
		infos.add(printInfo);
		try {
			this.addPrinters(infos);
		} catch (Exception e){
			Toast.makeText(this.printService, e.toString(), Toast.LENGTH_LONG).show();
			System.err.println(e.toString());
		}
	}
}