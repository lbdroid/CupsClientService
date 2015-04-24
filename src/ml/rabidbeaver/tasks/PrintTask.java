package ml.rabidbeaver.tasks;

import android.os.AsyncTask;
import android.printservice.PrintJob;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrintJob;
import org.cups4j.PrintRequestResult;
import org.cups4j.operations.AuthInfo;

public class PrintTask extends AsyncTask<Void, Void, Void>{
	
	private CupsClient client;
	private String queue;
	private CupsPrintJob cupsPrintJob;
	private AuthInfo auth;
	private Exception exception;
	private PrintRequestResult result;
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
	
	public void setJob(CupsPrintJob job, AuthInfo auth){
		this.cupsPrintJob = job;
		this.auth = auth;
        System.setProperty("java.net.preferIPv4Stack" , "true"); 
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
	
	public PrintRequestResult getResult(){
		return result;
	}
	
	public CupsPrintJob getCupsPrintJob(){
		return cupsPrintJob;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			result = client.print(queue, cupsPrintJob, auth);
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
