package ml.rabidbeaver.cupsprint;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.tasks.CancelJobTask;
import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprintservice.R;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary;
import ml.rabidbeaver.jna.cups_dest_s;
import ml.rabidbeaver.jna.cups_job_s;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class JobListActivity extends Activity implements GetPrinterListener{
	
	private PrintQueueConfig config;
	private Updater updater;
	private JobRecordAdapter recordAdapter;
	private TextView jobPrinter;
	private ListView jobsListView;
	private CupsClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.job_list_activity);
		Intent intent = getIntent();
		String nickname = intent.getStringExtra("printer");
		if (nickname == null){
			Util.showToast(this, "No printer selected");
			finish();
			return;
		}
		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(getBaseContext());
		config = dbconf.getPrinter(nickname);
		dbconf.close();
		if (config == null){
			Util.showToast(this, "Printer config not found");
			finish();
			return;
		}
		jobPrinter = (TextView) findViewById(R.id.jobPrinter);
		jobsListView = (ListView) findViewById(R.id.jobsListView);
		recordAdapter = new JobRecordAdapter(this);
		jobsListView.setAdapter(recordAdapter);
		try {
			client = new CupsClient(Util.getClientURL(config).getHost(), Util.getClientURL(config).getPort());
		} catch (Exception e){
			Util.showToast(this, e.toString());
			finish();
			return;
		}
		if (!(config.password.equals(""))){
			client.setUserPass(config.userName, config.password);
		}
		GetPrinterTask task = new GetPrinterTask(client, config.getPrintQueue(), false);
		task = new GetPrinterTask(client, Util.getQueue(config),true);
		task.setListener(this);
		try {
			task.execute().get(5000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e){
			Util.showToast(this, e.toString());
			finish();
			return;
		}
		Exception ex = task.getException();
		if (ex != null){
			Util.showToast(this, ex.toString());
			finish();
			return;
		}
		jobsListView.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
        	cups_job_s record = (cups_job_s) recordAdapter.getItem(position);
        	setOperation(record);
        	}
		});

	}

	@Override
	protected void onPause(){
		super.onPause();
		if (updater != null){
			updater.doStop();
		}
		updater = null;
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		updater = new Updater(this);
		new Thread(updater).start();;
	}
	
	public void setOperation(final cups_job_s record){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    String jobId = String.valueOf(record.id);
	    String[] items = {"Cancel Job"}; 
	    builder.setTitle("Job Id: " + jobId)
	           .setItems(items, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   doOperation(record, which);
	           }
	    });
	    AlertDialog dialog = builder.create();
	    dialog.show();
	}
	
	public void doOperation(cups_job_s record, int operation){
	    CancelJobTask.Operation taskOp = null;
	    switch (operation){
	    	case 0:
	    		taskOp = CancelJobTask.Operation.CANCEL;
	    		break;
	    }
	    if (taskOp != null){
	    	CancelJobTask task = 
	    			new CancelJobTask(this, client, config, taskOp, record.id);
	    	task.execute();
	    }
	}
	
	public void updateUI(final cups_job_s[] jobList){
		
		runOnUiThread(new Runnable(){
			
			public void run() {
				int length;
				if (jobList == null) length=0;
				else length = jobList.length;
				jobPrinter.setText(config.nickname + ": " + length + " " + "jobs");
				recordAdapter.setRecords(jobList);
				recordAdapter.notifyDataSetChanged();
			}
		});
	}
	
	public class Updater implements Runnable{
		
		private boolean stop = false;
		private Activity activity;
		
		public Updater(Activity activity){
			this.activity = activity;
		}
		
		public void doStop(){
			stop = true;
		}

		@Override
		public void run() {
			if (client == null){
				return;
			}
			int passes = 0;
			while (!stop){
				if (passes == 0){
					cups_job_s[] jobList;
					try {
						jobList = client.getJobs(config.queue, MlRabidbeaverJnaLibrary.CUPS_WHICHJOBS_ALL, false);
					}
					catch (Exception e){
						Util.showToast(activity, "CupsPrintService Jobs List\n" + e.toString());
						activity.finish();
						return;
					}
					if (!stop){
						updateUI(jobList);
					}
				}
				passes++;
				if (passes > 3){
					passes = 0;
				}
				try {
					Thread.sleep(1000);
				}catch (Exception e){
					return;
				}
			}
		}
	}

	@Override
	public void onGetPrinterTaskDone(cups_dest_s printer, List<JobOptions> jobOptions, Exception exception) {
		// do nothing
	}

}
