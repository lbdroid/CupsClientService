package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.List;

import ml.rabidbeaver.detect.PrinterRec;
import ml.rabidbeaver.detect.PrinterResult;
import ml.rabidbeaver.detect.PrinterUpdater;
import ml.rabidbeaver.jna.cups_dest_s;
import ml.rabidbeaver.jna.cups_option_s;
import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class PrinterEditActivity extends AppCompatActivity implements PrinterUpdater, GetPrinterListener{

	Spinner  protocol;
	EditText nickname;
	EditText host;
	EditText port;
	EditText queue;
	EditText userName;
	EditText password;
	CheckBox isDefault;
	String oldPrinter;
	List<JobOptions> printerOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.printer_add_edit_activity);
		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		
		ArrayList<String> protocols = new ArrayList<String>();
		protocols.add("http");
		protocols.add("https");
		
		oldPrinter = intent.getStringExtra("printer");
		if (oldPrinter == null) oldPrinter="";
		nickname = (EditText) findViewById(R.id.editNickname);
		protocol = (Spinner) findViewById(R.id.editProtocol);
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, protocols);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		protocol.setAdapter(aa);
		host = (EditText) findViewById(R.id.editHost);
		port = (EditText) findViewById(R.id.editPort);
		queue = (EditText) findViewById(R.id.editQueue);
		userName = (EditText) findViewById(R.id.editUserName);
		password = (EditText) findViewById(R.id.editPassword);

		isDefault = (CheckBox) findViewById(R.id.editIsDefault);
		
		Button saveBtn = (Button) findViewById(R.id.editSave);
		saveBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View view) {
				//savePrinter(v);
			    final PrintQueueConfHandler confdb = new PrintQueueConfHandler(getBaseContext());
			    String sNickname = nickname.getText().toString().trim();
			    if (!(sNickname.length()>0)){
			    	nickname.requestFocus();
			    	return;
			    }
			    if (checkExists(sNickname, confdb)){
			    	nickname.requestFocus();
			    	return;
			    }
			    String sHost = host.getText().toString().trim();
			    if (!(sHost.length()>0)){
			    	host.requestFocus();
			    	return;
			    }
			    String sPort = port.getText().toString().trim();
			    if (!(sPort.length()>0)){
			    	port.requestFocus();
			    	return;
			    }
			    if (!(Integer.parseInt(sPort)>0)){
			    	port.requestFocus();
			    	return;
			    }
			    String sQueue = queue.getText().toString().trim();
			    if (!(sQueue.length()>0)){
			    	queue.requestFocus();
			    	return;
			    }
			    String sUserName = userName.getText().toString().trim();
			    if (sUserName.equals("")){
			    	sUserName = "anonymous";
			    }
			    String sProtocol = (String) protocol.getSelectedItem();
			    String sPassword = password.getText().toString().trim();

			    final PrintQueueConfig conf = new PrintQueueConfig(sNickname, sProtocol, sHost, sPort, sQueue);
			    if (checkExists(conf.getPrintQueue(), confdb)){
			    	host.requestFocus();
			    	return;
			    }
			    conf.userName = sUserName;
			    conf.password = sPassword;
			    conf.isDefault = isDefault.isChecked();
			    conf.printerAttributes = printerOptions;
			    if ((conf.protocol.equals("http")) && (!(conf.password.equals("")))){
			        AlertDialog.Builder builder = new AlertDialog.Builder(PrinterEditActivity.this);
			        builder.setTitle("Warning: Using password with http protocol")
			        	.setMessage("This will result in both your username and password being sent over the network as plain text. "
			        		+ "Using the https protocol is reccomended for authentication.")
			        	.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			        		public void onClick(DialogInterface dialog, int id) {
			        			doSave(confdb, conf);
			        		}
			        	})
			        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        		public void onClick(DialogInterface dialog, int id) {}
			        	});
			        builder.create().show();
			    	return;
			    }
			    doSave(confdb, conf);
			}
		});
		
		Button testBtn = (Button) findViewById(R.id.editTest);
		testBtn.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View v) {
				testPrinter(v);
			}
		});

		if (!oldPrinter.contentEquals("")){
		    PrintQueueConfHandler dbconf = new PrintQueueConfHandler(getBaseContext());
		    PrintQueueConfig conf = dbconf.getPrinter(oldPrinter);
			dbconf.close();
		    if (conf != null){
		    	int size = protocols.size();
		    	int pos = 0;
		 		for (pos=0; pos<size; pos++){
		 			String test = protocols.get(pos);
		 			if (test.equals(conf.protocol)){
		 				protocol.setSelection(pos);
		 				break;
		 			}
		 		}
		 		nickname.setText(conf.nickname);
		    	host.setText(conf.host);
		    	port.setText(conf.port);
		    	queue.setText(conf.queue);
		    	userName.setText(conf.userName);
		    	password.setText(conf.password);
		 		isDefault.setChecked(conf.isDefault);
		 		 
		 		printerOptions = conf.printerAttributes; 

				findViewById(R.id.editSave).setEnabled(true);
		     }
		}
		if (oldPrinter.equals(""))
			port.setText("631");
	}

	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
	            finish();
	    }
	    return super.onContextItemSelected(item);
	 }

	private void alert(String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle("error");
		AlertDialog dialog = builder.create();	
		dialog.show();
	}
	
	private boolean checkExists(String name, PrintQueueConfHandler conf){
		
		if (oldPrinter.equals(name))
			return false;
		if (!conf.printerExists(name))
			return false;
		
		alert("Duplicate nickname: " + name);
		return true;
				
	}
	
	private void showResult(String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		       .setTitle(title);
		AlertDialog dialog = builder.create();	
		try {
			dialog.show();
		}catch (Exception e){}
	}
	
	public void testPrinter(View view){
		PrintQueueConfig printConfig = new PrintQueueConfig(
				nickname.getText().toString(),
				(String)protocol.getSelectedItem(),
					host.getText().toString(),
					port.getText().toString(),
					queue.getText().toString());
		
	    CupsClient client;
	    try {
	    	client = new CupsClient(Util.getClientURL(printConfig).getHost(), Util.getClientURL(printConfig).getPort());
	    }
	    catch (Exception e){
	    	showResult("Failed", e.getMessage());
	    	return;
	    }
   
	    String user = userName.getText().toString();
	    if (user.equals("")){
	    	user = "anonymous";
	    }
	    String passwd = password.getText().toString();
	    if (!(passwd.equals(""))){
	    	client.setUserPass(user,passwd);
	    }
	    
	    GetPrinterTask task = new GetPrinterTask(client, Util.getQueue(printConfig), false);
	    task.setListener(this);
	    task.execute();
	}
	
	@Override
	public void onGetPrinterTaskDone(cups_dest_s printer, List<JobOptions> printerOptions, Exception exception) {
		this.printerOptions = printerOptions;
		
		findViewById(R.id.editSave).setEnabled(true);
	    
		if (exception != null){
	    	showResult("Failed", exception.getMessage());
	    	return;
	    }
	    
	    if (printer == null){
	    	showResult("Failed", "Printer not found");
	    	return;
	    }
	    
	    cups_option_s opts = printer.options;
	    opts.read();
	    cups_option_s[] optsarr = (cups_option_s[]) opts.toArray(printer.num_options);
	    
	    String result = "queue: " + printer.name.getString(0);
	    for (int i=0; i<printer.num_options; i++){
	    	result += "\n"+ optsarr[i].name.getString(0) + ": " + optsarr[i].value.getString(0);
	    }
			    
		showResult("Success", result);
	}
	
	public void doSave(PrintQueueConfHandler confdb, PrintQueueConfig conf){
	     confdb.addOrUpdatePrinter(conf, oldPrinter);
	     CupsPrintFramework.getPrinterDiscovery().updateStaticConfig();
	     finish();
	}
	
	@Override
	public void getDetectedPrinter(PrinterResult results){
		List<PrinterRec> printers = results.getPrinters();
		if (printers.size() < 1){
			showResult("", "No printers found");
			return;
		}
		final ArrayAdapter<PrinterRec> aa = new ArrayAdapter<PrinterRec>(
				this, android.R.layout.simple_list_item_1,printers); 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select printer");
		builder.setAdapter(aa, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PrinterRec printer = aa.getItem(which);
				int size = protocol.getCount();
				int i;
				for (i=0; i<size; i++){
					if (protocol.getItemAtPosition(i).equals(printer.getProtocol())){
						protocol.setSelection(i);
						break;
					}
				}
				nickname.setText(printer.getNickname());
				protocol.setSelection(i);
				host.setText(printer.getHost());
				port.setText(String.valueOf(printer.getPort()));
				queue.setText(printer.getQueue());					
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
