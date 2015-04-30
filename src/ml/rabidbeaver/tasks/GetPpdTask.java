package ml.rabidbeaver.tasks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsjni.cups_dest_s;

public class GetPpdTask implements Runnable{

	private cups_dest_s printer;
	private Exception exception;
	private byte[] md5;
	private CountDownLatch latch;
	private GetPpdListener taskListener;
	private CupsPpd cupsPpd;

	public GetPpdTask(cups_dest_s printer, CupsPpd cupsPpd, byte[] md5){
		this.printer = printer;
		this.cupsPpd = cupsPpd;
		this.md5 = md5;
	}
	
	public void setPpdTaskListener(GetPpdListener listener){
		this.taskListener = listener;
	}
	

	@Override
	public void run() {
		try {
			cupsPpd.createPpdRec(printer, md5);
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
	
	public void execute(final int priority){
		
		Runnable runner = new Runnable(){

			@Override
			public void run() {
				Thread t = new Thread(this);
				t.setPriority(priority);
				t.start();
				latch = new CountDownLatch(1);
				try{
					latch.await(7000, TimeUnit.MILLISECONDS);
				}
				catch (Exception e){
					exception = e;
					System.err.println(e.toString());
				}
				taskListener.onGetPpdTaskDone(exception);
			}
		};
		runner.run();
	}
	
	public void get(int priority){
		Thread t = new Thread(this);
		t.setPriority(priority);
		t.start();
		latch = new CountDownLatch(1);
		try{
			latch.await(7000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e){
			exception = e;
			System.err.println(e.toString());
		}
	}
}
