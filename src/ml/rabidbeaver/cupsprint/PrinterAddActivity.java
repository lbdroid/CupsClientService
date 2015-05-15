package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.cupsprintservice.R;
import ml.rabidbeaver.detect.HostScanTask;
import ml.rabidbeaver.detect.MdnsScanTask;
import android.view.Menu;
import android.view.MenuItem;

public class PrinterAddActivity extends PrinterEditActivity {
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.printer_add_edit_menu, menu);
		return true;
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.scanhost:
	    	    String user = userName.getText().toString();
	    	    if (user.equals("")){
	    	    	user = "anonymous";
	    	    }
	    	    String passwd = password.getText().toString();
	    		new HostScanTask(this, this, user, passwd).execute();
	    		break;
	    	case R.id.scanmdns:
	    		new MdnsScanTask(this, this).execute();
	    		break;
	    	case android.R.id.home:
	            finish();
	    }
	    return super.onContextItemSelected(item);
	 }
}
