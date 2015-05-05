package ml.rabidbeaver.cupsprint;

import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.cups_dest_s;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class MimeTypesActivity extends Activity implements GetPrinterListener {

	PrintQueueConfig printConfig;
	GetPrinterTask task;
	
	/* TODO:
	 * The onCreate function is waiting for the printer task to complete, which
	 * means that the UI thread is frozen until the task completes. This causes
	 * unresponsiveness and occasionally BLACK SCREEN while waiting. Everything
	 * in the onCreate function from immediately below instantiation of the task
	 * should be broken off into another thread, and the finishing changes on
	 * the activity should be completed after the activity is already showing.
	 */	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mime_types);
		Intent intent = getIntent();
		String sPrinter = intent.getStringExtra("printer");
	    printConfig = new PrintQueueConfHandler(getBaseContext()).getPrinter(sPrinter);
	    if (printConfig == null){
			Util.showToast(this, "Config for " + sPrinter + " not found");
			finish();
	    	return;
		}
		CupsClient client;
	    try {
	    	client = new CupsClient(Util.getClientURL(printConfig).getHost(), Util.getClientURL(printConfig).getPort());
	    }
	    catch (Exception e){
	    	Util.showToast(this, e.getMessage());
	    	finish();
	    	return;
	    }
	    if (!(printConfig.getPassword().equals(""))){
	    	client.setUserPass(printConfig.getUserName(), printConfig.getPassword());
	    }
	    task = new GetPrinterTask(client, Util.getQueue(printConfig),true);
	    task.setListener(this);
	    try {
	    	//TODO: revisit this timeout setting. When it was set at 5000 (5 seconds)
	    	// it was timing out on mobile/remote over ssh tunnel.
	    	task.execute().get(15000, TimeUnit.MILLISECONDS);
	    }
	    catch (Exception e){
	    	Util.showToast(this, e.toString());
	    	finish();
	    	return;
	    }
	    Exception exception = task.getException();
	    
	    if (exception != null){
	    	Util.showToast(this, exception.getMessage());
	    	finish();
	    	return;
	    }
	    
	    cups_dest_s printer = task.getPrinter();
	    if (printer == null){
	    	Util.showToast(this, printConfig.nickname + " not found");
	    	finish();
	    	return;
	    }

	    String[] mimetypes = client.getAttribute(printer, "document-format-supported");
	    if (mimetypes == null || mimetypes.length == 0){
	    	Util.showToast(this, "Unable to get mime types for " + printConfig.nickname);
	    	finish();
	    	return;
	    }
	    
		TextView mimeList = (TextView) findViewById(R.id.mimeList);
		String S = printConfig.nickname + "\n\n"; 
	    for(String type: mimetypes){
	    	S = S + type + "\n";
	    }
		mimeList.setText(S);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}


	@Override
	public void onGetPrinterTaskDone(cups_dest_s printer, Exception exception) {
	}

}
