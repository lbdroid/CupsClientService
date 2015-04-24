package ml.rabidbeaver.tasks;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.operations.AuthInfo;

import android.content.Context;
import android.os.AsyncTask;


public class GetPrinterTask extends AsyncTask<Void, Void, Void>{

	protected CupsClient client;
	protected String queue;
	protected boolean extended;
	protected Exception exception;
	protected GetPrinterListener listener;
	protected CupsPrinter printer;
	protected AuthInfo auth;
	protected Context ctx;
	
	public GetPrinterTask(CupsClient client, AuthInfo auth, String queue, boolean extended){
		super();
		this.client = client;
		this.auth = auth;
		this.queue = queue;
		this.extended = extended;
	}
	
	public void setListener(GetPrinterListener listener){
		this.listener = listener;
	}
	
	public CupsPrinter getPrinter(){
		return printer;
	}
	
	public Exception getException(){
		return exception;
	}
	
	@Override
	protected Void doInBackground(Void... params){
		try {
			this.printer = client.getPrinter(queue, auth, extended);
		}
		catch (Exception e){
			exception = e;
			System.err.println(e.toString());
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void v){
		if (!this.isCancelled()){
			listener.onGetPrinterTaskDone(printer, exception);
		}
	}
}
