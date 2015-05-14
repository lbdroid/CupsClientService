package ml.rabidbeaver.printservice;

import android.os.Bundle;
import android.print.PrintJobInfo;
import android.printservice.PrintService;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.cupsprint.Util;
import ml.rabidbeaver.cupsprintservice.R;

public class AdvancedOptionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.advanced_options_activity);
		Intent intent = getIntent();

		final PrintJobInfo jobInfo = (PrintJobInfo) intent.getParcelableExtra("android.intent.extra.print.PRINT_JOB_INFO");
		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(this);
		PrintQueueConfig config = dbconf.getPrinter(jobInfo.getPrinterId().getLocalId());
		dbconf.close();
		if (config == null){
			Util.showToast(this, "Printer configuration not found");
			finish();
			return;
		}

		TableLayout layout = (TableLayout) findViewById(R.id.advancedSettingsLayout);
		
		//TODO: add elements to UI here

        Button oKButton = new Button(this);
        oKButton.setText("OK");
        oKButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

        		PrintJobInfo.Builder builder = new PrintJobInfo.Builder(jobInfo);
        		
        		//TODO: read values from UI and putAdvancedOption's here;
        		builder.putAdvancedOption("finishings", "fartypants");
        		builder.putAdvancedOption("number-up", "4");
        		
        		PrintJobInfo newPrintJobInfo = builder.build();
        		
        		Intent result = new Intent();
        		result.putExtra(PrintService.EXTRA_PRINT_JOB_INFO, newPrintJobInfo);
        		setResult(Activity.RESULT_OK, result);
        		
        		finish();
            }
        });

        TableRow buttonRow = new TableRow(this);
		buttonRow.addView(oKButton);
                
        layout.addView(buttonRow,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}
}
