package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsjni.cups_dest_s;

public interface GetPrinterListener {
	
	public void onGetPrinterTaskDone(cups_dest_s printer, Exception exception);

}
