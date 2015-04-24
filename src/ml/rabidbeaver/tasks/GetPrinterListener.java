package ml.rabidbeaver.tasks;

import org.cups4j.CupsPrinter;

public interface GetPrinterListener {
	
	public void onGetPrinterTaskDone(CupsPrinter printer, Exception exception);

}
