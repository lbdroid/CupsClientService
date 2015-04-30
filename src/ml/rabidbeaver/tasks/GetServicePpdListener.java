package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;

public interface GetServicePpdListener {

	public void onGetServicePpdTaskDone(CupsPpd cupsPpd, PrintQueueConfig config, Exception exception);
}
