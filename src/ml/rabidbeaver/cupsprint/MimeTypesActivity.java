package ml.rabidbeaver.cupsprint;

import java.util.List;

import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.TextView;

public class MimeTypesActivity extends AppCompatActivity {

	PrintQueueConfig printConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mime_types_activity);
		
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		Intent intent = getIntent();
		String sPrinter = intent.getStringExtra("printer");
		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(getBaseContext());
	    printConfig = dbconf.getPrinter(sPrinter);
		dbconf.close();
	    
	    if (printConfig == null){
			Util.showToast(this, "Config for " + sPrinter + " not found");
			finish();
	    	return;
		}

	    List<JobOptions> attributes = printConfig.getPrinterAttributes();
	    String mimetypes = printConfig.nickname + "\n";
	    for (int i=0; i<attributes.size(); i++)
	    	if (attributes.get(i).name.equals("document-format-supported"))
	    		mimetypes += "\n" + attributes.get(i).value;

		TextView mimeList = (TextView) findViewById(R.id.mimeList);

		mimeList.setText(mimetypes);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
	    switch (menuItem.getItemId()) {
	        case android.R.id.home:
	            finish();
	    }
	    return (super.onOptionsItemSelected(menuItem));
	}
}
