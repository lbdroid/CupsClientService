package ml.rabidbeaver.cupsprint;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ml.rabidbeaver.discovery.PrinterDiscovery;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class CupsPrintFramework extends Application{
    
	private static CupsPrintFramework instance;
	private static PrinterDiscovery printerDiscovery;
	private static SecretKey secretKey;
	private static final String PREF_FILE = "userData";
	private static final String USER_KEY = "userKey";


    public static CupsPrintFramework getInstance() {
        return instance;
    }

    public static PrinterDiscovery getPrinterDiscovery(){
    	return printerDiscovery;
    }

    public static Context getContext(){
        return instance.getApplicationContext();
    }
    
    public static SecretKey getSecretKey(){
    	return secretKey;
    }
    

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        setSecretKey(this.getApplicationContext());
    	printerDiscovery = new PrinterDiscovery();
    	printerDiscovery.updateStaticConfig();
    }

    private void setSecretKey(Context context){
	    SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
		String encoded = sharedPreferences.getString(USER_KEY, null);
		if (encoded != null){
			secretKey = new SecretKeySpec(Base64.decode(encoded, Base64.DEFAULT), "AES");
			return;
		}

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		    keyGenerator.init(128);
		    secretKey = keyGenerator.generateKey();
		}catch (Exception e){
			System.err.println(e.toString());
			secretKey = null;
			return;
		}
	    encoded = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
	    Editor editor = sharedPreferences.edit();
	    editor.putString(USER_KEY, encoded);
	    editor.commit();
	}
}
