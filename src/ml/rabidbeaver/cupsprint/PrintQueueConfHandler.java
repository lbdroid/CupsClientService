package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.Cipher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

public class PrintQueueConfHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "printers.db";

	public PrintQueueConfHandler(Context context){
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		String create = "CREATE TABLE printers (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
				+ "name VARCHAR, host VARCHAR, protocol VARCHAR, port INTEGER, queue VARCHAR, "
				+ "username VARCHAR, password VARCHAR, orientation VARCHAR, fittopage INTEGER, "
				+ "nooptions INTEGER, extensions VARCHAR, resolution VARCHAR, "
				+ "def INTEGER DEFAULT 0);";
		db.execSQL(create);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Since this is the first version, there is nothing to upgrade.		
	}
	
	public String getDefaultPrinter(){
		SQLiteDatabase db = this.getReadableDatabase();
		// SELECT name FROM printers WHERE def = 1:
		Cursor cursor = db.query("printers", new String[]{"name"}, "def = ?", new String[]{"1"}, null, null, null);
		String defprinter=null;
		if (cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			defprinter = cursor.getString(0);
			cursor.close();
		}
		return defprinter;
	}
	
	private void setDefaultPrinter(SQLiteDatabase db, String printer){
		ContentValues values = new ContentValues();
		values.put("def", 1);
		db.update("printers", values, "name = ?", new String[]{printer});
		values = new ContentValues();
		values.put("def", 0);
		db.update("printers", values, "name != ?", new String[]{printer});
	}

	public boolean printerExists(String name){
		SQLiteDatabase db = this.getReadableDatabase();
		// SELECT id FROM printers WHERE name = name:
		Cursor cursor = db.query("printers", new String[]{"id"}, "name = ?", new String[]{name}, null, null, null);
		return (cursor != null && cursor.getCount() > 0);
	}
	
	public void removePrinter(String printer){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("printers", "name = ?", new String[]{printer});
		db.close();
	}
	
	public void addOrUpdatePrinter(PrintQueueConfig config, String oldPrinter){
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("name", config.nickname);
		values.put("host", config.host);
		values.put("protocol", config.protocol);
		values.put("port", config.port);
		values.put("queue", config.queue);
		values.put("username", config.userName);
		values.put("password", encrypt(config.password));
		values.put("orientation", config.orientation);
		values.put("fittopage", config.imageFitToPage);
		values.put("nooptions", config.noOptions);
		values.put("extensions", config.extensions);
		values.put("resolution", config.resolution);
		
		if (printerExists(oldPrinter))
			db.update("printers", values, "name = ?", new String[]{"oldPrinter"});
		else {
			db.insert("printers", null, values);			
			if (config.isDefault) setDefaultPrinter(db,config.nickname);
		}
	}
	
	private ArrayList<String> getPrinters(){
		SQLiteDatabase db = this.getReadableDatabase();
		// SELECT name FROM printers;
		Cursor cursor = db.query("printers", new String[]{"name"}, null, null, null, null, null);
		ArrayList<String> printerList = new ArrayList<String>();
		if (cursor.moveToFirst()){
			do {
				printerList.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null) cursor.close();

		Collections.sort(printerList);
		return printerList;
	}
	
	public ArrayList<String> getPrintQueueConfigs(){
		return getPrinters();
	}

	public PrintQueueConfig getPrinter(String name){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query("printers", new String[]{"host","protocol","port","queue",
				"username","password","orientation","fittopage","nooptions","extensions",
				"resolution","showin","def"}, "name = ?", new String[]{name}, null, null, null);
		
		if (cursor == null || cursor.getCount() < 1) return null;
		
		cursor.moveToFirst();
		String host = cursor.getString(0);
		String protocol = cursor.getString(1);
		String port = Integer.toString(cursor.getInt(2));
		String queue = cursor.getString(3);
		PrintQueueConfig pqc = new PrintQueueConfig(name, protocol, host, port, queue);
		pqc.userName = cursor.getString(4);
		pqc.password = decrypt(cursor.getString(5));
		pqc.orientation = cursor.getString(6);
		pqc.imageFitToPage = cursor.getInt(7)!=0;
		pqc.noOptions = cursor.getInt(8)!=0;
		pqc.extensions = cursor.getString(9);
		pqc.resolution = cursor.getString(10);
		pqc.isDefault = cursor.getInt(12)!=0;
		return pqc;
	}

	private String encrypt(String data){
		if (data.equals("")){
			return "";
		}
		try {
			Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, CupsPrintApp.getSecretKey());
	        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
	        return new String(Base64.encode(encryptedBytes, Base64.DEFAULT));
		}catch (Exception e){
			System.err.println(e.toString());
			return "";
		}
		
	}
	
	private String decrypt(String data){
		if (data.equals("")){
			return "";
		}
		try {
			Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.DECRYPT_MODE, CupsPrintApp.getSecretKey());
	        byte[] decryptedBytes = cipher.doFinal(Base64.decode(data, Base64.DEFAULT));
	        return new String(decryptedBytes);
		}catch (Exception e){
			System.err.println(e.toString());
			return "";
		}
	}
}
