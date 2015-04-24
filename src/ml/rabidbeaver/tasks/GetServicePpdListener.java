package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsprint.PrintQueueConfig;

import org.cups4j.ppd.CupsPpd;

public interface GetServicePpdListener {

	public void onGetServicePpdTaskDone(CupsPpd cupsPpd, PrintQueueConfig config, Exception exception);
}
