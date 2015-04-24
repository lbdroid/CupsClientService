package ml.rabidbeaver.tasks;

import ml.rabidbeaver.cupsprint.Util;

import org.cups4j.CupsClient;
import org.cups4j.PrintRequestResult;
import org.cups4j.operations.AuthInfo;

import android.app.Activity;
import android.os.AsyncTask;

public class CancelJobTask extends AsyncTask<Void, Void, Void>{

	public enum Operation{
		CANCEL, HOLD, RELEASE
	}
	
	private Activity activity;
	private CupsClient client;
	private AuthInfo auth;
	private int jobId;
	private Operation op;
	private PrintRequestResult result;
	
	public CancelJobTask(Activity activity, CupsClient client, AuthInfo auth, Operation op, int jobId){
		super();
		this.activity = activity;
		this.client = client;
		this.auth = auth;
		this.op = op;
		this.jobId = jobId;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			switch (op){
			case CANCEL:
				result = client.cancelJob(jobId, auth);
				break;
			case HOLD:
				result = client.holdJob(jobId, auth);
				break;
			case RELEASE:
				result = client.releaseJob(jobId, auth);
				break;
			}
			Util.showToast(activity, result.getResultDescription());
		}catch (Exception e){
			Util.showToast(activity, e.toString());
		}
		return null;
	}

}
