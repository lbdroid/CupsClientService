package ml.rabidbeaver.discovery;

public interface PrinterDiscoveryListener {
	
	public void onPrinterAdded(PrinterDiscoveryInfo info);
	
	public void onPrinterRemoved(PrinterDiscoveryInfo info);

}
