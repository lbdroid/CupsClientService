package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.Util;
import ml.rabidbeaver.cupsjni.CupsClient;
//import org.cups4j.PrintRequestResult;

import android.app.Activity;
import android.os.AsyncTask;

public class CancelJobTask extends AsyncTask<Void, Void, Void>{

	public enum Operation{
		CANCEL
	}
	
	private Activity activity;
	private CupsClient client;
	private int jobId;
	private Operation op;
	private boolean result;
	private PrintQueueConfig config;
	
	public CancelJobTask(Activity activity, CupsClient client, PrintQueueConfig config, Operation op, int jobId){
		super();
		this.activity = activity;
		this.client = client;
		this.op = op;
		this.jobId = jobId;
		this.config=config;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			switch (op){
			case CANCEL:
				result = client.cancelJob(config.queue, jobId);
				break;
			}
			Util.showToast(activity, Boolean.toString(result)/*TODO */);
		}catch (Exception e){
			Util.showToast(activity, e.toString());
		}
		return null;
	}

}
