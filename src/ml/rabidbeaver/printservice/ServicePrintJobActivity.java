package ml.rabidbeaver.printservice;

import java.util.Iterator;

import android.os.Bundle;
import android.print.PrintJobInfo;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import ml.rabidbeaver.cupscontrols.CupsTableLayout;
import ml.rabidbeaver.cupsprint.CupsPrintApp;
import ml.rabidbeaver.cupsprint.PpdGroupsActivity;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueIniHandler;
import ml.rabidbeaver.cupsprint.Util;

import ml.rabidbeaver.cupsprintservice.R;

public class ServicePrintJobActivity extends Activity {

	private CupsTableLayout layout;
	private static CupsPpd cupsPpd;
	Button oKButton;
	Button moreButton;
	boolean moreClicked = false;
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
				
		PrintQueueIniHandler ini = new PrintQueueIniHandler(CupsPrintApp.getContext());
		PrintQueueConfig config = ini.getPrinter(nickname);
		if (config == null){
			Util.showToast(this, "Printer configuration not found");
			finish();
			return;
		}
		if (RBPrintService.ppdJobId == jobInfo.getId()){
			cupsPpd = RBPrintService.jobPpd;
		}
		else {
			CupsPpd ppd = RBPrintService.capabilities.get(nickname);
			if (ppd != null){
				if (!(config.getPassword().equals(""))){
					//TODO ?? auth = new AuthInfo(CupsPrintApp.getContext(), config.getUserName(), config.getPassword());
				}
				cupsPpd = new CupsPpd();
				cupsPpd.setPpdRec(ppd.getPpdRec().deepCloneUILists());
				RBPrintService.ppdJobId = jobInfo.getId();
				RBPrintService.jobPpd = cupsPpd;
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
            	if (!layout.update())
                	return;
                String stdAttrs = cupsPpd.getCupsStdString();
        		setCupsString(stdAttrs);
            }
        });
        moreButton = getButton("More...");
		moreButton.setEnabled(true);
        moreButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doGroup();
            }
        });
		buttonRow = new TableRow(this);
		buttonRow.addView(moreButton);
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
	
	public static CupsPpd getPpd(){
		return cupsPpd;
	}
	
	private void setStdOpts(){
		
		for (PpdSectionList group: cupsPpd.getPpdRec().getStdList()){
		
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
		}
		
	}

	private void setControls(){
		layout = (CupsTableLayout) findViewById(R.id.advancedSettingsLayout);
		layout.setShowName(false);
		layout.reset();
		for (PpdSectionList group: cupsPpd.getPpdRec().getStdList()){
			layout.addSection(group);
		}
		layout.addView(buttonRow,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		uiSet = true;
	}
	
	private void setCupsString(String stdAttrs){
		String ppdAttrs = "";
        if (moreClicked){
        	if (cupsPpd != null)
        		ppdAttrs = cupsPpd.getCupsExtraString();
        }
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
	
	private void doGroup(){
		Intent intent = new Intent(this, PpdGroupsActivity.class);
		intent.putExtra("op", "service");
		startActivity(intent);
		moreClicked = true;
	}
	
	private Button getButton(String defaultVal){
		Button btn = new Button(this);
		btn.setText(defaultVal);
		return btn;
	}
}
