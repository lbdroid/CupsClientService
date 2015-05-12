package ml.rabidbeaver.tasks;

import java.util.List;

import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.jna.cups_dest_s;

public interface GetPrinterListener {
	void onGetPrinterTaskDone(cups_dest_s printer, List<JobOptions> printerOptions, Exception exception);
}
