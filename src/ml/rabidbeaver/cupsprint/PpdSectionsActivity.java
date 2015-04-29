package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.cupscontrols.CupsTableLayout;
import ml.rabidbeaver.printservice.ServicePrintJobActivity;

import ml.rabidbeaver.cupsprintservice.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PpdSectionsActivity extends Activity {

	private PpdSectionList group;
	boolean uiSet = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ppd_sections);
		Intent intent = getIntent();
		int index = intent.getIntExtra("section", 0);
		String op = intent.getStringExtra("op");
		if (op == null){
			op = "";
		}
		if (op.equals("service")){
			group = ServicePrintJobActivity.getPpd().getPpdRec().getExtraList().get(index);
		}
		else {
			group = PrintJobActivity.getPpd().getPpdRec().getExtraList().get(index);
		}
		setControls();
	}

	@Override
	public void onConfigurationChanged (Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if (uiSet){
			setControls();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.aboutmenu, menu);
		return true;
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case R.id.about:
	    		Intent intent = new Intent(this, AboutActivity.class);
	    		intent.putExtra("printer", "");
	    		startActivity(intent);
	    		break;
	    }
	    return super.onContextItemSelected(item);
	 }

	private void setControls(){
		final CupsTableLayout layout = (CupsTableLayout) findViewById(R.id.sectionsViewLayout);
		layout.reset();
		layout.addSection(group);
		TableRow row = new TableRow(this);
		row.addView(new TextView(this));

		Button btn = new Button(this);
		btn.setText("OK");
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!layout.update())
                	return;
                finish();
            }
        });
		row.addView(btn);
		layout.addView(row,new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		uiSet = true;
	}

}
