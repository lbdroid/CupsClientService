package ml.rabidbeaver.tasks;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsjni.cups_dest_s;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;

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
			//TODO: WTF does this even do?
			//cups_dest_s printer = new cups_dest_s(new URL(config.getPrintQueue()));
			//cupsPpd = new CupsPpd();
			//cupsPpd.createPpdRec(printer, md5);
			//cupsPpd.setServiceResolution(config.getResolution());
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
