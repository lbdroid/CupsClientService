package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.printservice.ServicePrintJobActivity;
import ml.rabidbeaver.cupsjni.PpdSectionList;

import ml.rabidbeaver.cupsprintservice.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PpdGroupsActivity extends Activity {

	private String op;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ppd_groups);
		final ListView groupListView = (ListView) findViewById(R.id.groupListView);
		groupListView.setClickable(true);
		Intent intent = getIntent();
		op = intent.getStringExtra("op");
		if (op == null){
			op = "";
		}
		ArrayAdapter<PpdSectionList> aa;
		if (op.equals("service")){
			aa = new ArrayAdapter<PpdSectionList>(this, 
					android.R.layout.simple_list_item_1, ServicePrintJobActivity.getPpd().getPpdRec().getExtraList());
		}
		else {
			aa = new ArrayAdapter<PpdSectionList>(this, 
					android.R.layout.simple_list_item_1, PrintJobActivity.getPpd().getPpdRec().getExtraList());
		}

		groupListView.setAdapter(aa);
		
		groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				doSection(position);
			}
		});
	}

	private void doSection(int index){
		Intent intent = new Intent(this, PpdSectionsActivity.class);
		intent.putExtra("section", index);
		intent.putExtra("op", op);
		startActivity(intent);
	}
}
