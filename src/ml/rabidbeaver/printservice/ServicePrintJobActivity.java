package ml.rabidbeaver.printservice;

import java.util.List;

import android.os.Bundle;
import android.print.PrintJobInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.cupsprint.Util;
import ml.rabidbeaver.cupsprintservice.R;

public class ServicePrintJobActivity extends Activity {

	private static List<JobOptions> cupsJobOptions;
	Button oKButton;
	PrintJobInfo jobInfo;
	boolean uiSet = false;
	TableRow buttonRow;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_settings);
		Intent intent = getIntent();
		/*Bundle bundle = intent.getExtras();
	    String string = "";
		for (String key : bundle.keySet()) {
	        string += " " + key + " => " + bundle.get(key) + ";";
	    }
	    string += " }Bundle";
	    System.out.println(string);
	    */
	    //PrinterInfo printerInfo = (PrinterInfo) intent.getParcelableExtra("android.intent.extra.print.EXTRA_PRINTER_INFO");
		//PrinterInfo.Builder builder = new PrinterInfo.Builder(printerInfo);
		jobInfo = (PrintJobInfo) intent.getParcelableExtra("android.intent.extra.print.PRINT_JOB_INFO");
		String nickname = jobInfo.getPrinterId().getLocalId();
		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(CupsPrintApp.getContext());
		PrintQueueConfig config = dbconf.getPrinter(nickname);
		dbconf.close();
		if (config == null){
			Util.showToast(this, "Printer configuration not found");
			finish();
			return;
		}
		if (CupsPrintService.jobId == jobInfo.getId()){
			cupsJobOptions = CupsPrintService.jobOptions;
		}
		else {
			List<JobOptions> jobOptions = CupsPrintService.capabilities.get(nickname);
			if (jobOptions != null){
				if (!(config.getPassword().equals(""))){
					//TODO ?? auth = new AuthInfo(CupsPrintApp.getContext(), config.getUserName(), config.getPassword());
				}
				//cupsJobOptions = new ArrayList<JobOptions>();
				//cupsJobOptions.setPpdRec(jobOptions.getPpdRec().deepCloneUILists());
				cupsJobOptions = jobOptions;
				CupsPrintService.jobId = jobInfo.getId();
				CupsPrintService.jobOptions = cupsJobOptions;
			}
			else {
				Util.showToast(this, "Unable to create advanced options");
				finish();
				return;
			}
		}
        setStdOpts();

        oKButton = getButton("OK");
        oKButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	oKButton.setFocusableInTouchMode(true);
            	oKButton.requestFocus();
            	//if (!layout.update())
                //	return;
                // TODO String stdAttrs = cupsJobOptions.getCupsStdString();
        		setCupsString(null);//stdAttrs);
            }
        });
        /*moreButton = getButton("More...");
		moreButton.setEnabled(true);
        moreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doGroup();
            }
        });*/
		buttonRow = new TableRow(this);
		//buttonRow.addView(moreButton);
		buttonRow.addView(oKButton);
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
		getMenuInflater().inflate(R.menu.advanced_settings, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
		    Intent intent = new Intent();
		    intent.putExtra("android.intent.extra.print.PRINT_JOB_INFO", jobInfo);
		    setResult(Activity.RESULT_OK, intent);
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}	
	
	public static List<JobOptions> getJobOptions(){
		return cupsJobOptions;
	}
	
	private void setStdOpts(){
		
		/* TODO for (PpdSectionList group: cupsJobOptions.getPpdRec().getStdList()){
		
			Iterator<PpdItemList> it = group.iterator();
			while(it.hasNext()){
				PpdItemList section = it.next();
				if (section.getName().equals("orientation-requested")){
					it.remove();
				}
				else if (section.getName().equals("PageSize")){
					it.remove();
				}
				else if (section.getName().equals("PageRegion")){
					it.remove();
				}
				else if (section.getName().equals("fit-to-page")){
					section.setSavedValue("true");
				}
				else if (section.getName().equals("copies")){
					it.remove();
				}
				else if (section.getName().equals("page-ranges")){
					it.remove();
				}
			}
		}*/
		
	}

	private void setControls(){
		/* TODO layout = (CupsTableLayout) findViewById(R.id.advancedSettingsLayout);
		layout.setShowName(false);
		layout.reset();
		for (PpdSectionList group: cupsJobOptions.getPpdRec().getStdList()){
			layout.addSection(group);
		}
		layout.addView(buttonRow,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		*/
		uiSet = true;
	}
	
	private void setCupsString(String stdAttrs){
		String ppdAttrs = "";
        //if (moreClicked){
        	// TODO if (cupsJobOptions != null)
        		//ppdAttrs = cupsJobOptions.getCupsExtraString();
        //}
        if (!(stdAttrs.equals("")) && !(ppdAttrs.equals(""))){
        	stdAttrs = stdAttrs + "#" + ppdAttrs;
        }
        else{
        	stdAttrs = stdAttrs + ppdAttrs;
        }

        PrintJobInfo.Builder jobInfoBuilder = new PrintJobInfo.Builder(jobInfo);
        jobInfoBuilder.putAdvancedOption("CupsString", stdAttrs);
	    PrintJobInfo newInfo = jobInfoBuilder.build();
	    Intent intent = new Intent();
	    intent.putExtra("android.intent.extra.print.PRINT_JOB_INFO", newInfo);
	    setResult(Activity.RESULT_OK, intent);
	    finish();        
	}
	
	/*private void doGroup(){
		Intent intent = new Intent(this, PpdGroupsActivity.class);
		intent.putExtra("op", "service");
		startActivity(intent);
		moreClicked = true;
	}*/
	
	private Button getButton(String defaultVal){
		Button btn = new Button(this);
		btn.setText(defaultVal);
		return btn;
	}
}
