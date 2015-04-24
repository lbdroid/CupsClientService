package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.operations.AuthInfo;

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
	    	client = new CupsClient(Util.getClientURL(printConfig));
	    }
	    catch (Exception e){
	    	Util.showToast(this, e.getMessage());
	    	finish();
	    	return;
	    }
	    AuthInfo auth = null;
	    if (!(printConfig.getPassword().equals(""))){
	    	auth = new AuthInfo(printConfig.getUserName(), printConfig.getPassword());
	    }
	    task = new GetPrinterTask(client, auth, Util.getQueue(printConfig),true);
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
	    
	    CupsPrinter printer = task.getPrinter();
	    if (printer == null){
	    	Util.showToast(this, printConfig.nickname + " not found");
	    	finish();
	    	return;
	    }
	    
	    ArrayList<String> mimeTypes = printer.getSupportedMimeTypes();
	    if (mimeTypes.size() == 0){
	    	Util.showToast(this, "Unable to get mime types for " + printConfig.nickname);
	    	finish();
	    	return;
	    }
	    
		TextView mimeList = (TextView) findViewById(R.id.mimeList);
		String S = printConfig.nickname + "\n\n"; 
	    for(String type: mimeTypes){
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
	public void onGetPrinterTaskDone(CupsPrinter printer, Exception exception) {
	}

}
