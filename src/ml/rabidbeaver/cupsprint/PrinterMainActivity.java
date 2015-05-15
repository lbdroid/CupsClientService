package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;

import ml.rabidbeaver.cupsprintservice.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PrinterMainActivity extends AppCompatActivity {

	ListView printersListView;
	ArrayList<String> printersArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.printer_main_activity);

		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		printersListView=(ListView) findViewById(R.id.printersListView);
		printersListView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	        	setOperation(position);
	        	}
			});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
	    switch (menuItem.getItemId()) {
	        case android.R.id.home:
	            finish();
	    }
	    return (super.onOptionsItemSelected(menuItem));
	}
	
	@Override
	public void onStart(){
		super.onStart();
		
		PrintQueueConfHandler dbconf = new PrintQueueConfHandler(getBaseContext());
		printersArray = dbconf.getPrintQueueConfigs();
		dbconf.close();
		if (printersArray.size() == 0){
			new AlertDialog.Builder(this)
			.setTitle("")
			.setMessage("No printers are configured. Add new printer?")
			.setIcon(android.R.drawable.ic_input_add)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	addPrinter();
			    }})
			 .setNegativeButton(android.R.string.no, null).show();	
			
		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
			android.R.layout.simple_list_item_1, printersArray);
		printersListView.setAdapter(aa);
	}
	
	private void setOperation(final int index) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    String[] items = {"Edit", "Delete", "Jobs", "Mime Types"}; 
	    final String nickname = printersArray.get(index);
	    builder.setTitle(nickname)
	           .setItems(items, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   doOperation(nickname, which);
	           }
	    });
	    AlertDialog dialog = builder.create();
	    dialog.show();
	}
	
	private void doOperation(final String nickname, final int op){
		
		if (op == 0){
			Intent intent = new Intent(this, PrinterEditActivity.class);
			intent.putExtra("printer", nickname);
			startActivity(intent);
		}
		else if (op == 1){
			
			new AlertDialog.Builder(this)
			.setTitle("Confim")
			.setMessage("Delete " + nickname + "?")
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	doDelete(nickname);
			    }})
			 .setNegativeButton(android.R.string.no, null).show();	
		}
		else if (op == 2){
			Intent intent = new Intent(this, JobListActivity.class);
			intent.putExtra("printer", nickname);
			startActivity(intent);
		}
		else if (op == 3){
			Intent intent = new Intent(this, MimeTypesActivity.class);
			intent.putExtra("printer", nickname);
			startActivity(intent);
			
		}
	}

	private void doDelete(String printer){
		System.out.println("delete called");
		PrintQueueConfHandler confdb = new PrintQueueConfHandler(getBaseContext());
		confdb.removePrinter(printer);
		printersArray = confdb.getPrintQueueConfigs();
		confdb.close();
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, printersArray);
		printersListView.setAdapter(aa);
		CupsPrintFramework.getPrinterDiscovery().updateStaticConfig();
	}
	
	public void addPrinter(){
		Intent intent = new Intent(this, PrinterEditActivity.class);
		intent.putExtra("printer", "");
		startActivity(intent);
	}
	
}
