package ml.rabidbeaver.cupsprint;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;

import android.app.Activity;
import android.widget.Toast;

public class Util {
	
	public static URL getURL(String urlStr) throws MalformedURLException{
		
		UrlValidator urlValidator = new UrlValidator();
		if (urlValidator.isValid(urlStr)){
			try {
				return new URL(urlStr);
			}catch (Exception e){}
		}
		throw new MalformedURLException("Invalid URL\n" + urlStr);
	}
	
	public static String getQueue(PrintQueueConfig printConfig){
		return "/printers/" + printConfig.queue;
	}
	
	public static String getClientUrlStr(PrintQueueConfig printConfig){
		return printConfig.protocol + "://" + 
				printConfig.host + ":" + 
				printConfig.port;
	}
	
	public static String getPrinterUrlStr(PrintQueueConfig printConfig){
		return getClientUrlStr(printConfig) +
				"/printers/" + printConfig.queue;
	}
	
	public static URL getClientURL(PrintQueueConfig printConfig) throws MalformedURLException{
		String urlStr = getClientUrlStr(printConfig);
		return getURL(urlStr);
	}
	
	public static URL getPrinterURL(PrintQueueConfig printConfig) throws MalformedURLException{
		String urlStr = getPrinterUrlStr(printConfig);
		return getURL(urlStr);
	}
	
	
	public static void showToast(final Activity activity, final String toast)
	{
	    activity.runOnUiThread(new Runnable() {
	        public void run()
	        {
	            Toast.makeText(activity, toast, Toast.LENGTH_LONG).show();
	        }
	    });
	}
}
