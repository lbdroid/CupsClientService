package ml.rabidbeaver.printservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprint.CupsPrintFramework;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.discovery.PrinterDiscoveryInfo;
import ml.rabidbeaver.discovery.PrinterDiscoveryListener;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;
import android.widget.Toast;

public class CupsPrinterDiscoverySession extends PrinterDiscoverySession implements PrinterDiscoveryListener {

	private CupsPrintService printService;
	
	public CupsPrinterDiscoverySession(CupsPrintService printService){
		Log.d("CUPSPRINTERDISCOVERYSESSION","init()");
		this.printService = printService;
	}
	
	@Override
	public void onDestroy(){
		Log.d("CUPSPRINTERDISCOVERYSESSION","onDestroy()");
	}
	
	@Override
	public void onStartPrinterDiscovery(List<PrinterId> arg0) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onStartPrinterDiscovery()");
		Map<String, PrinterDiscoveryInfo> printerMap = CupsPrintFramework.getPrinterDiscovery().addDiscoveryListener(this);
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
		removePrinters(printerIds);
	 }

	@Override
	public void onStopPrinterDiscovery() {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onStopPrinterDiscovery()");
		CupsPrintFramework.getPrinterDiscovery().removeDiscoveryListener(this);
	}
	
	@Override
	public void onStartPrinterStateTracking(PrinterId printerId) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onStartPrinterStateTracking()");
		String nickName = printerId.getLocalId();

		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(CupsPrintFramework.getContext());
		PrintQueueConfig config = dbconf.getPrinter(nickName);
		dbconf.close();
		if (config != null)
			setPrinterCapabilities(nickName, config);
	}

	@Override
	public void onStopPrinterStateTracking(PrinterId arg0) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onStopPrinterStateTracking()");
	}

	@Override
	public void onValidatePrinters(List<PrinterId> arg0) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onValidatePrinters()");
	}

	@Override
	public void onPrinterAdded(final PrinterDiscoveryInfo info) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onPrinterAdded()");
		Handler handler = new Handler(CupsPrintFramework.getContext().getMainLooper());
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
				PrinterInfo printerInfo = createPrinterInfo(info);
				if (printerInfo != null){
					printers.add(printerInfo);
					CupsPrinterDiscoverySession.this.addPrinters(printers);
				}
			}
		};
		handler.post(runnable);
	}

	@Override
	public void onPrinterRemoved(final PrinterDiscoveryInfo info) {
		Log.d("CUPSPRINTERDISCOVERYSESSION","onPrinterRemoved()");
		Handler handler = new Handler(CupsPrintFramework.getContext().getMainLooper());
		Runnable runnable = new Runnable(){
			@Override
			public void run() {
				List<PrinterId> ids = new ArrayList<PrinterId>();
				PrinterId id = printService.generatePrinterId(info.getNickname());
				ids.add(id);
				CupsPrinterDiscoverySession.this.removePrinters(ids);
			}
		};
		handler.post(runnable);
	}
	
	private PrinterInfo createPrinterInfo(PrinterDiscoveryInfo info){
		Log.d("CUPSPRINTERDISCOVERYSESSION","createPrinterInfo()");
		PrinterId id = printService.generatePrinterId(info.getNickname());
		PrinterInfo.Builder builder = new PrinterInfo.Builder(id, info.getNickname(), PrinterInfo.STATUS_IDLE);
		try{
			return builder.build();
		}catch (Exception e){
			System.err.println(e.toString());
			return null;
		}
	}
	
	@SuppressLint("DefaultLocale")
	private void setPrinterCapabilities(String nickname, PrintQueueConfig config){
		Log.d("CUPSPRINTERDISCOVERYSESSION","setPrinterCapabilities()");
		PrinterId id = printService.generatePrinterId(nickname);
		PrinterInfo.Builder infoBuilder = new PrinterInfo.Builder(id, nickname, PrinterInfo.STATUS_IDLE);
		PrinterCapabilitiesInfo.Builder capBuilder = new PrinterCapabilitiesInfo.Builder(id);
		
		List<JobOptions> attributes = config.getPrinterAttributes();
		
		String def_media = "na_letter_8.5x11in";
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("media-default")) def_media = attributes.get(i).value;
		
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("media-supported")){
				String newmedia = attributes.get(i).value;
				String[] parts = newmedia.split("_");
				String medianame = (parts[0]+" "+parts[1]).toUpperCase();
				boolean inches = parts[2].contains("in");
				parts[2] = parts[2].replace("in", "").replace("mm", "");
				String[] dimens = parts[2].split("x");
				int w, h;
				w = (int) (Float.parseFloat(dimens[0]) * 1000);
				h = (int) (Float.parseFloat(dimens[1]) * 1000);
				if (!inches){
					w *= 0.0393701;
					h *= 0.0393701;
				}
				capBuilder.addMediaSize(new PrintAttributes.MediaSize(attributes.get(i).value, medianame, w, h), attributes.get(i).value.equals(def_media));
			}
		
		String def_res = "600x600";
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("printer-resolution-default")) def_res = attributes.get(i).value;
		
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("printer-resolution-supported")){
				String[] res = attributes.get(i).value.split("x");
				int x = Integer.parseInt(res[0]);
				int y = Integer.parseInt(res[1]);
				capBuilder.addResolution(new PrintAttributes.Resolution(attributes.get(i).value, res[0]+" DPI", x, y), attributes.get(i).value.equals(def_res));
			}

		int def_color = PrintAttributes.COLOR_MODE_MONOCHROME;
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("print-color-mode-default"))
				def_color = attributes.get(i).value.equals("color")?PrintAttributes.COLOR_MODE_COLOR:PrintAttributes.COLOR_MODE_MONOCHROME;

		int colormode = def_color;
		for (int i=0; i<attributes.size(); i++)
			if (attributes.get(i).name.equals("print-color-mode-supported")){
				if (attributes.get(i).value.equals("color")) colormode |= PrintAttributes.COLOR_MODE_COLOR;
				if (attributes.get(i).value.equals("monochrome")) colormode |= PrintAttributes.COLOR_MODE_MONOCHROME;
			}
		capBuilder.setColorModes(colormode, def_color);
		
/*		int leftMils=0, topMils=0, rightMils=0, bottomMils=0;
		for (int i=0; i<attributes.size(); i++){
			if (attributes.get(i).name.equals("media-bottom-margin-supported"))
				bottomMils = (int) (Float.parseFloat(attributes.get(i).value));
			if (attributes.get(i).name.equals("media-left-margin-supported"))
				leftMils = (int) (Float.parseFloat(attributes.get(i).value));
			if (attributes.get(i).name.equals("media-right-margin-supported"))
				rightMils = (int) (Float.parseFloat(attributes.get(i).value));
			if (attributes.get(i).name.equals("media-top-margin-supported"))
				topMils = (int) (Float.parseFloat(attributes.get(i).value));
		}
		PrintAttributes.Margins margins = new PrintAttributes.Margins(leftMils, topMils, rightMils, bottomMils);
		capBuilder.setMinMargins(margins);*/
		capBuilder.setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0));
		
		PrinterCapabilitiesInfo caps = null;
		PrinterInfo printInfo = null;
		try {
			caps = capBuilder.build();
			infoBuilder.setCapabilities(caps);
			printInfo = infoBuilder.build();
		}
		catch (Exception e){
			Toast.makeText(this.printService, e.toString(), Toast.LENGTH_LONG).show();
			Log.d("CUPSPRINTERDISCOVERYSESSION",e.toString());
			return;
		}

		List<PrinterInfo> infos = new ArrayList<PrinterInfo>();
		infos.add(printInfo);
		try {
			this.addPrinters(infos);
		} catch (Exception e){
			Toast.makeText(this.printService, e.toString(), Toast.LENGTH_LONG).show();
			Log.d("CUPSPRINTERDISCOVERYSESSION",e.toString());
		}
	}
}
