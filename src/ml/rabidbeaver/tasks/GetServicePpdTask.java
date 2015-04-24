package ml.rabidbeaver.tasks;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.cupsprint.PrintQueueConfig;

import org.cups4j.CupsPrinter;
import org.cups4j.operations.AuthInfo;
import org.cups4j.ppd.CupsPpd;

public class GetServicePpdTask implements Runnable{

	private PrintQueueConfig config;
	private byte[] md5;
	private CountDownLatch latch;
	private GetServicePpdListener taskListener;
	private CupsPpd cupsPpd;
	private Exception exception;
	private AuthInfo auth;

	public GetServicePpdTask(PrintQueueConfig config, AuthInfo auth, byte[] md5){
		this.config = config;
		this.auth = auth;
		this.md5 = md5;
	}
	
	public void setPpdTaskListener(GetServicePpdListener listener){
		this.taskListener = listener;
	}
	

	@Override
	public void run() {
		try {
			CupsPrinter printer = new CupsPrinter(new URL(config.getPrintQueue()));
			cupsPpd = new CupsPpd(auth);
			cupsPpd.createPpdRec(printer, md5);
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
