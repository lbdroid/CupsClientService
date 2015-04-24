package ml.rabidbeaver.detect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

public class MdnsScanTask extends AsyncTask<Void, Integer, PrinterResult>
implements ProgressUpdater{

MdnsServices services;
Context context;
ProgressDialog pd;
PrinterUpdater printerUpdater;
boolean stopped = false;

public MdnsScanTask(Context context, PrinterUpdater printerUpdater){
	this.context = context;
	this.printerUpdater = printerUpdater;
}

@Override
protected PrinterResult doInBackground(Void... arg0) {
	services = new MdnsServices(this);
	PrinterResult results = services.scan(context);
	return results;
}

@Override
protected void onPreExecute(){
	pd = new ProgressDialog(context);
	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	pd.setIndeterminate(false);
	pd.setCanceledOnTouchOutside(false);
	pd.setMax(100);
	pd.setTitle("Scanning mDNS");
	pd.setOnDismissListener(new OnDismissListener(){
		@Override
        public void onDismiss(DialogInterface dialog) {
			doStop();
        }			
	});
	pd.show();
}

protected void onProgressUpdate(Integer... progress) {
	pd.setProgress(progress[0]);
}

@Override
protected void onPostExecute(PrinterResult result){
	services = null;
	if (pd != null)
		pd.dismiss();
	if (!stopped)
		printerUpdater.getDetectedPrinter(result);
}

@Override
public void DoUpdate(int value) {
	this.publishProgress(value);
}

public void doStop(){
	stopped = true;
	if (!(services == null)){
		services.stop();
	}

}

}

