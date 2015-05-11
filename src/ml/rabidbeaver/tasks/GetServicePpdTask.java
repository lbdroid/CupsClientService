package ml.rabidbeaver.tasks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.jna.cups_dest_s;

public class GetServicePpdTask implements Runnable{

	private PrintQueueConfig config;
	private byte[] md5;
	private CountDownLatch latch;
	private GetServicePpdListener taskListener;
	private CupsPpd cupsPpd;
	private Exception exception;

	public GetServicePpdTask(PrintQueueConfig config, byte[] md5){
		this.config = config;
		this.md5 = md5;
	}
	
	public void setPpdTaskListener(GetServicePpdListener listener){
		this.taskListener = listener;
	}
	

	@Override
	public void run() {
		try {
			CupsClient c = new CupsClient(config.host,Integer.valueOf(config.port));
			cups_dest_s printer = c.getPrinter(config.queue, false);
			cupsPpd = new CupsPpd();
			cupsPpd.createPpdRec(c, printer, md5);
			cupsPpd.setServiceResolution(config.getResolution());
			latch.countDown();
		}
		catch (Exception e){
			exception = e;
			System.err.println(e.toString());
		}
	}
	
	public Exception getException(){
		return exception;
	}
	
	public void get(boolean async, int priority){
		Thread t = new Thread(this);
		t.setPriority(priority);
		t.start();
		latch = new CountDownLatch(1);
		if (async){
			try{
				latch.await(7000, TimeUnit.MILLISECONDS);
			}
			catch (Exception e){
				exception = e;
				System.err.println(e.toString());
			}
		taskListener.onGetServicePpdTaskDone(cupsPpd, config, exception);
		}
	}
}
