package ml.rabidbeaver.cupsprint;

import java.util.List;
import ml.rabidbeaver.cupsjni.JobOptions;
import ml.rabidbeaver.cupsprintservice.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

public class MimeTypesActivity extends Activity {

	PrintQueueConfig printConfig;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mime_types);
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
}
