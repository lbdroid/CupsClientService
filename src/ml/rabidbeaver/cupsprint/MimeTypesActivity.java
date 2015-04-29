package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsClient.cups_dest_t;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class MimeTypesActivity extends Activity implements GetPrinterListener {

	PrintQueueConfig printConfig;
	GetPrinterTask task;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mime_types);
		Intent intent = getIntent();
		String sPrinter = intent.getStringExtra("printer");
		PrintQueueIniHandler ini = new PrintQueueIniHandler(getBaseContext());
	    printConfig = ini.getPrinter(sPrinter);
	    if (printConfig == null){
			Util.showToast(this, "Config for " + sPrinter + " not found");
			finish();
	    	return;
		}
		CupsClient client;
	    try {
	    	client = new CupsClient(Util.getClientURL(printConfig).toString());
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
	    	task.execute().get(5000, TimeUnit.MILLISECONDS);
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
	    
	    cups_dest_t printer = task.getPrinter();
	    if (printer == null){
	    	Util.showToast(this, printConfig.nickname + " not found");
	    	finish();
	    	return;
	    }
	    
	    //TODO ArrayList<String> mimeTypes = printer.getOption("document-format-supported");//.getSupportedMimeTypes();
	    if (true){//TODO mimeTypes.size() == 0){
	    	Util.showToast(this, "Unable to get mime types for " + printConfig.nickname);
	    	finish();
	    	return;
	    }
	    
		TextView mimeList = (TextView) findViewById(R.id.mimeList);
		String S = printConfig.nickname + "\n\n"; 
	    //TODO for(String type: mimeTypes){
	    //	S = S + type + "\n";
	    //}
		//mimeList.setText(S);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}


	@Override
	public void onGetPrinterTaskDone(cups_dest_t printer, Exception exception) {
	}

}
