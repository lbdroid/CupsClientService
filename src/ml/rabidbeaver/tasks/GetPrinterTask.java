package ml.rabidbeaver.tasks;

import java.util.List;

import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.jna.cups_dest_s;
import android.content.Context;
import android.os.AsyncTask;

public class GetPrinterTask extends AsyncTask<Void, Void, Void>{

	protected CupsClient client;
	protected String queue;
	protected boolean extended;
	protected Exception exception;
	protected GetPrinterListener listener;
	protected cups_dest_s printer;
	protected List<JobOptions> printerOptions;
	protected Context ctx;
	
	public GetPrinterTask(CupsClient client, String queue, boolean extended){
		super();
		this.client = client;
		this.queue = queue;
		this.extended = extended;
	}
	
	public void setListener(GetPrinterListener listener){
		this.listener = listener;
	}
	
	public cups_dest_s getPrinter(){
		return printer;
	}
	
	public Exception getException(){
		return exception;
	}
	
	@Override
	protected Void doInBackground(Void... params){
		try {
			this.printer = client.getPrinter(queue, extended);
			this.printerOptions = client.getPrinterOptions(this.printer);
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
			listener.onGetPrinterTaskDone(printer, printerOptions, exception);
		}
	}
}
