package ml.rabidbeaver.printservice;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.print.PrintJobInfo;
import android.printservice.PrintService;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprint.PrintQueueConfig;
import ml.rabidbeaver.cupsprint.PrintQueueConfHandler;
import ml.rabidbeaver.cupsprint.Util;
import ml.rabidbeaver.cupsprintservice.R;

public class AdvancedOptionsActivity extends Activity {

	@SuppressWarnings("unchecked") // needed in order to create an array of lists.
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
		
		List<JobOptions> printerAttributes = config.getPrinterAttributes();
		final List<JobOptions> setJobOptions = new ArrayList<JobOptions>();

		List<String> jobOptions = new ArrayList<String>();
		JobOptions opt;
		for (int i=0; i< printerAttributes.size(); i++){
			opt = printerAttributes.get(i);
			if (opt.name.equals("job-settable-attributes-supported")){
				jobOptions.add(opt.value);
			}
		}
		
		jobOptions.remove("page-ranges");
		jobOptions.remove("orientation-requested");
		jobOptions.remove("print-color-mode");
		jobOptions.remove("media");
		jobOptions.remove("copies");
		jobOptions.remove("job-name");
		jobOptions.remove("job-priority"); // priority specification is ambiguous
		
		List<?>[] supp_opt = new List<?>[jobOptions.size()];
		String[] def_opt = new String[jobOptions.size()];
		String[] name_opt = new String[jobOptions.size()];
		for (int i=0; i< jobOptions.size(); i++){
			supp_opt[i] = new ArrayList<String>();
			def_opt[i] = "";
			name_opt[i] = jobOptions.get(i);
		}
		
		for (int i=0; i< printerAttributes.size(); i++){
			for (int j=0; j< jobOptions.size(); j++){
				if (printerAttributes.get(i).name.equals(name_opt[j]+"-supported"))
					((ArrayList<String>)supp_opt[j]).add(printerAttributes.get(i).value);
				if (printerAttributes.get(i).name.equals(name_opt[j]+"-default"))
					def_opt[j]=printerAttributes.get(i).value;
			}
		}

		for (int i=0; i< jobOptions.size(); i++){
			// TODO: if the number of supported options is a RANGE "#-#", create a number field with +/-
			// else (this is a string...) create a spinner.
			
			// for now just create a spinner.
			if (supp_opt[i].size() > 1){ // only create the spinner if there are multiple options to select from
				int defidx = -1;
				List<JobOptions> spinlist = new ArrayList<JobOptions>();
				for (int j=0; j< supp_opt[i].size(); j++){
					spinlist.add(new JobOptions(name_opt[i],((ArrayList<String>)supp_opt[i]).get(j)));
					if (((ArrayList<String>)supp_opt[i]).get(j).equals(def_opt[i])) defidx = j;
				}

				ArrayAdapter<JobOptions> ab = new ArrayAdapter<JobOptions>(this, android.R.layout.simple_spinner_item, spinlist);
				Spinner newspinner = new Spinner(this);
				newspinner.setAdapter(ab);
				if (defidx > -1) newspinner.setSelection(defidx);
				else spinlist.add(0, new JobOptions(name_opt[i],"printer default"));
				spinlist.get(newspinner.getSelectedItemPosition()).defopt=true;
				newspinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						JobOptions j = (JobOptions) ((Spinner)parent).getItemAtPosition(position);
						Log.d("SPINNERITEMSELECTED",j.name+": "+j.value);
						setJobOptions.remove(j);
						if (!j.defopt) setJobOptions.add(j);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {}
				});
			
				TableRow tr = new TableRow(this);
				TextView label = new TextView(this);
				label.setText(name_opt[i]);
				tr.addView(label);
				tr.addView(newspinner);
			
				layout.addView(tr);
			}
		}

        Button oKButton = new Button(this);
        oKButton.setText("OK");
        oKButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

        		PrintJobInfo.Builder builder = new PrintJobInfo.Builder(jobInfo);
        		
        		for (int i=0; i<setJobOptions.size(); i++)
        			builder.putAdvancedOption(setJobOptions.get(i).name, setJobOptions.get(i).value);
        		
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
