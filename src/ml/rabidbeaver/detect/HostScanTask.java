package ml.rabidbeaver.detect;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;

public class HostScanTask extends AsyncTask<Void, Integer, PrinterResult>
 implements ProgressUpdater{

IPTester services;
Context context;
PrinterUpdater printerUpdater;
ProgressDialog pd;
boolean stopped = false;
String username = "";
String password = "";

public HostScanTask(Context context, PrinterUpdater updater, String username, String password){
	this.context = context;
	this.printerUpdater = updater;
	if (username != null){
		this.username = username;
	}
	if (password != null){
		this.password = password;
	}
}

@Override
protected PrinterResult doInBackground(Void... arg0) {
	services = new IPTester(this);
	String ip = null;
	try {
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
					ip = inetAddress.getHostAddress();
				}
			}
		}
	} catch (Exception e) {
		System.out.println(e.toString());
	}
	if (ip == null){
		return null;
	}
	return services.getPrinters(context, ip, 24, 631, username, password);
}

@Override
protected void onPreExecute(){
	pd = new ProgressDialog(context);
	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	pd.setIndeterminate(false);
	pd.setCanceledOnTouchOutside(false);
	pd.setMax(100);
	pd.setTitle("Scanning Hosts");
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

