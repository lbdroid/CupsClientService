package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsjni.CupsClient.cups_dest_t;

public interface GetPrinterListener {
	
	public void onGetPrinterTaskDone(cups_dest_t printer, Exception exception);

}
