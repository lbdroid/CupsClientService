package ml.rabidbeaver.cupsprint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ml.rabidbeaver.cupscontrols.CupsTableLayout;
import ml.rabidbeaver.tasks.GetPpdListener;
import ml.rabidbeaver.tasks.GetPpdTask;
import ml.rabidbeaver.tasks.GetPrinterListener;
import ml.rabidbeaver.tasks.GetPrinterTask;
import ml.rabidbeaver.cupsjni.CupsClient;
import ml.rabidbeaver.cupsjni.CupsPrintJob;
import ml.rabidbeaver.cupsjni.PpdItemList;
import ml.rabidbeaver.cupsjni.PpdSectionList;
import ml.rabidbeaver.cupsjni.cups_dest_s;
import ml.rabidbeaver.cupsjni.CupsPpd;
import ml.rabidbeaver.cupsprintservice.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;

public class PrintJobActivity extends Activity 
	implements GetPrinterListener, GetPpdListener{

	PrintQueueConfig printerConfig;
	cups_dest_s cupsPrinter;
	CupsClient cupsClient;
	Uri data;
	static CupsPpd cupsppd;
	Button printButton;
	Button moreButton;
	boolean isImage;
	boolean moreClicked = false;
	CupsTableLayout layout;
	String mimeType = "";
	boolean mimeTypeSupported = false;
	boolean acceptMimeType = false;
	String mimeMessage = "";
	String fileName = "";
	String extension = "";
	Map<String, String> attributes;
	GetPrinterTask getPrinterTask;
	GetPpdTask getPpdTask;
	boolean uiSet = false;
	TableRow buttonRow;
	Context ctx;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_job);
		ctx=this.getApplicationContext();
		Intent intent = getIntent();
		String type = intent.getStringExtra("type");
		String sPrinter;
		if (type.equals("static")){
			sPrinter = intent.getStringExtra("printer");
			printerConfig = new PrintQueueConfHandler(getBaseContext()).getPrinter(sPrinter);
		}
		else {
			sPrinter = intent.getStringExtra("name");
			printerConfig = new PrintQueueConfig(
					intent.getStringExtra("name"),
					intent.getStringExtra("protocol"),
					intent.getStringExtra("host"),
					intent.getStringExtra("port"),
					intent.getStringExtra("queue"));
			printerConfig.userName = "anonymous";
			printerConfig.password = "";
		}
	    if (printerConfig == null){
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("Config for " + sPrinter + " not found")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	finish();
			    }})
			 .show();	
	    	return;
	    }
		data = intent.getData();
		if (data == null){
			this.doFinish("File URI is null");
			return;
		}
		fileName = getFileName();
		String [] fileParts = fileName.split("\\.");
		String ext = "";
		if (fileParts.length > 1){
			ext = fileParts[fileParts.length -1].toLowerCase(Locale.ENGLISH);
		}
		if (!ext.equals("")){
			mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		}
		if (mimeType == null){
			mimeType = "";
		}
		if (mimeType.equals("")){
			mimeType = intent.getStringExtra("mimeType");
		}
		try {
			cupsClient = new CupsClient(Util.getClientURL(printerConfig).getHost(), Util.getClientURL(printerConfig).getPort());
		}catch (Exception e){
			this.doFinish("Invalid ULR " + Util.getClientUrlStr(printerConfig));
			return;
		}

		if (!(printerConfig.password.equals(""))){
			cupsClient.setUserPass(printerConfig.userName, printerConfig.password);
		}
		getPrinterTask = new GetPrinterTask(cupsClient, Util.getQueue(printerConfig),true);
		getPrinterTask.setListener(this);
		try {
			getPrinterTask.execute().get(5000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e){
			doFinish(e.toString());
			return;
		}
		
		cupsPrinter = getPrinterTask.getPrinter();
		getPrinterTask = null;
		if (cupsPrinter == null){
			doFinish("Get printer failed");
			return;
		}

		//TODO: Probably should create a printer data class to store more information about
		// the printer than what is available in cups_dest_s structure.
		String[] supportedMimeTypes = cupsClient.getAttribute(cupsPrinter,"document-format-supported");
		for (int i=0; i<supportedMimeTypes.length && !mimeTypeSupported && mimeType != null && mimeType.length()>0; i++){
			if (supportedMimeTypes[i].equals(mimeType))
				mimeTypeSupported = true;
		}
		if (!mimeTypeSupported) setAcceptMimeType(mimeType, extension);

		if (mimeType.contains("image"))
			isImage = true;
		else
			isImage = false;

		if (!(printerConfig.password.equals(""))){
			cupsClient.setUserPass(printerConfig.userName, printerConfig.password);
		}
		cupsppd = new CupsPpd();
		if (printerConfig.noOptions){
			if (mimeTypeSupported){
				setStdOpts();
				setPrint(cupsppd.getCupsStdString());
				return;
			}
		}
 
		printButton = getButton("Print");
        printButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	printButton.setFocusableInTouchMode(true);
            	printButton.requestFocus();
            	if (!layout.update())
                	return;
                String stdAttrs = cupsppd.getCupsStdString();
        		setPrint(stdAttrs);
            }
        });

        moreButton = getButton("More...");
		moreButton.setEnabled(true);
        moreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doGroup();
            }
        });
        byte[] md5 = null;
        GetPpdTask getPpdTask = new GetPpdTask(cupsClient, cupsPrinter, cupsppd, md5);
        getPpdTask.get(Thread.NORM_PRIORITY);
		//should check exception here
		setStdOpts();
		buttonRow = new TableRow(this);
		buttonRow.addView(moreButton);
		buttonRow.addView(printButton);
        setControls();
	}
	
	@Override
	public void onConfigurationChanged (Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if (uiSet){
			setControls();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}
	
	public static CupsPpd getPpd(){
		return cupsppd;
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	private void setStdOpts(){
		
		for (PpdSectionList group: cupsppd.getPpdRec().getStdList()){
				for (PpdItemList section: group){
					if (section.getName().equals("fit-to-page")){
						if (isImage)
							section.setSavedValue("true");
					}
					else if(section.getName().equals("orientation-requested")){
						if (!(printerConfig.orientation.equals(""))){
							section.setSavedValue(printerConfig.orientation);
							section.setDefaultValue("-1");
						}
					}
				}
			}
	}
	
	private void doGroup(){
		Intent intent = new Intent(this, PpdGroupsActivity.class);
		startActivity(intent);
		moreClicked = true;
	}
	
	private Button getButton(String defaultVal){
		Button btn = new Button(this);
		btn.setText(defaultVal);
		return btn;
	}
	
	private void setControls(){
		layout = (CupsTableLayout) findViewById(R.id.printjobLayout);
		layout.reset();
		layout.setShowName(false);
		for (PpdSectionList group: cupsppd.getPpdRec().getStdList()){
			layout.addSection(group);
		}
		layout.addView(buttonRow,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		uiSet = true;
	}
	
	private void setPrint(String stdAttrs){
		String ppdAttrs = "";
        if (moreClicked){
        	if (cupsppd != null)
        		ppdAttrs = cupsppd.getCupsExtraString();
        }
        if (!(stdAttrs.equals("")) && !(ppdAttrs.equals(""))){
        	stdAttrs = stdAttrs + "#" + ppdAttrs;
        }
        else{
        	stdAttrs = stdAttrs + ppdAttrs;
        }
	    if (!(stdAttrs.equals(""))){
	    	attributes = new HashMap<String, String>();
	    	attributes.put("job-attributes", stdAttrs);
	    }
	    if (!(printerConfig.password).equals("")){
	    	cupsClient.setUserPass(printerConfig.userName, printerConfig.password);
	    }
        doPrint();
	}
	
	private void setAcceptMimeType(String mimeType, String ext){
		acceptMimeType = false;
		String [] extensions = printerConfig.extensions.split(" ");
		for (String item : extensions){
			if (mimeType.equals(item)){
				acceptMimeType = true;
				return;
			}
		}
		String msg = printerConfig.getPrintQueue() + " \ndoes not support " + mimeType + "\n\nContinue?";
		msg = msg + "\n\nNote. If this printer does support files with the " + ext + " extension, ";
		msg = msg + "you may wish to add " + ext + " to this printers extensions"; 
		new AlertDialog.Builder(this)
		.setTitle("Unspported mime type")
		.setMessage(msg)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

		    public void onClick(DialogInterface dialog, int whichButton) {
		    	acceptMimeType = true;
		    }})
		 .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	finish();
			    }})
		 .show();	
	}
	
	public void showToast(final String toast)
	{
	    runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(PrintJobActivity.this, toast, Toast.LENGTH_LONG).show();
	        }
	    });
	}
	
	public String getFileName(){
		String fileName = "";
		if ("content".equals(data.getScheme())){
            try {
            	//Cursor cursor = getContentResolver().query(data, null, null, null, null);
            	Cursor cursor = getContentResolver().query(data, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            	cursor.moveToFirst();
           		fileName = cursor.getString(0);
            	cursor.close();
            	}
            catch (Exception e){
            	fileName = data.getPath();
            }
		}
		else {
			fileName = data.getPath();
		}
		if (fileName == null){
			return "";
		}
        String[] fileParts = fileName.split("/");
        if (fileParts.length > 0)
        	fileName = fileParts[fileParts.length-1];
		
		return fileName;
	}
	
	public void doPrint(){
		
        Thread thread = new Thread(){
	        @Override
	        public void run() {
	        	try {
	        		FileInputStream file;
	        		try {
	        			file = (FileInputStream) getContentResolver().openInputStream(data);
	        		} catch (FileNotFoundException e) {
	        			showToast("CupsPrint error\n " + e.toString());
	        			System.out.println(e.toString());
	        			setResult(500);
	        			finish();
	        			return;
	        		}
		      			
	        		CupsPrintJob job = new CupsPrintJob(file, fileName);
	                
	        		if (attributes != null){
	        			job.setAttributes(attributes);
	        		}

	                int printResult = cupsClient.print(cupsPrinter, job);
	                if (printResult < 1) throw new Exception("Print job failed");
	            	showToast("CupsPrint\n" + fileName + "\n");
	        	}
	            catch (Exception e){
	            	showToast("CupsPrint error:\n" + e.toString());
	                System.out.println(e.toString());
	            }
	        }
        };
		thread.start();
		setResult(500);
		finish();
	}
	
	public void doFinish(String msg){
    	if (msg != "")
    	showToast(msg);
    	setResult(500);
    	finish();
	}

	@Override
	public void onGetPrinterTaskDone(cups_dest_s printer, Exception exception) {
	}

	@Override
	public void onGetPpdTaskDone(Exception exception) {
	}

}
