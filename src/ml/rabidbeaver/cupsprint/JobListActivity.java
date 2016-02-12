package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.tasks.CancelJobTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsprintservice.R;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary;
import ml.rabidbeaver.jna.cups_job_s;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class JobListActivity extends AppCompatActivity {
	
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
		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
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
		jobsListView.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
        	cups_job_s record = (cups_job_s) recordAdapter.getItem(position);
        	setOperation(record);
        	}
		});

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
	    switch (menuItem.getItemId()) {
	        case android.R.id.home:
	            finish();
	    }
	    return (super.onOptionsItemSelected(menuItem));
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if (client != null) client.cleanup();
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
			try {
			    client = new CupsClient(Util.getClientURL(config).getHost(), Util.getClientURL(config).getPort(), config.tunneluuid, config.getTunnelPort(), config.tunnelfallback, activity);
		    } catch (Exception e){
			    Util.showToast(activity, e.toString());
			    finish();
			    return;
		    }
			if (client == null) return;
			if (!(config.password.equals(""))) client.setUserPass(config.userName, config.password);
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
}