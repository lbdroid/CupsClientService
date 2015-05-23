package ml.rabidbeaver.tasks;

import android.os.AsyncTask;
import android.printservice.PrintJob;
import ml.rabidbeaver.cupsjni.CupsPrintJob;
import ml.rabidbeaver.cupsjni.CupsClient;

public class PrintTask extends AsyncTask<Void, Void, Void>{
	
	private CupsClient client;
	private String queue;
	private CupsPrintJob cupsPrintJob;
	private Exception exception;
	private int result;
	private PrintTaskListener listener;	
	private PrintJob servicePrintJob;
	
	public PrintTask(CupsClient client, String queue){
		super();
		this.client = client;
		this.queue = queue;
	}
	
	public void setListener(PrintTaskListener listener){
		this.listener = listener;
	}
	
	public void setJob(CupsPrintJob job){
		this.cupsPrintJob = job;
	}
	
	public void setServicePrintJob(PrintJob servicePrintJob){
		this.servicePrintJob = servicePrintJob;
	}

	public android.printservice.PrintJob getServicePrintJob(){
		return this.servicePrintJob;
	}
	
	public Exception getException(){
		return exception;
	}
	
	public int getResult(){
		return result;
	}
	
	public CupsPrintJob getCupsPrintJob(){
		return cupsPrintJob;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			result = client.print(queue, cupsPrintJob);
			client.cleanup();
		}catch (Exception e){
			this.exception = e;
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void v){
			listener.onPrintTaskDone(this);
	}
}
