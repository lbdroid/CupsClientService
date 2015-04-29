package ml.rabidbeaver.cupsprint;

import java.util.ArrayList;
import java.util.List;

import ml.rabidbeaver.cupsjni.CupsPrintJobAttributes;
import ml.rabidbeaver.cupsprintservice.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JobRecordAdapter extends BaseAdapter{

	Activity activity;
	List<CupsPrintJobAttributes> records;
	
	public JobRecordAdapter(Activity activity){
		this.activity = activity;
		records = new ArrayList<CupsPrintJobAttributes>();
	}
	
	public void setRecords(List<CupsPrintJobAttributes> records){
		this.records = records;
	}
	
	@Override
	public int getCount() {
		return records.size();
	}

	@Override
	public Object getItem(int position) {
		return records.get(position);
	}

	@Override
	public long getItemId(int position) {
		return records.get(position).getJobID();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_job_list_row, null);
        }
        try {
        	TextView id = (TextView) convertView.findViewById(R.id.jobJobId);
        	TextView name = (TextView) convertView.findViewById(R.id.jobJobName);
        	TextView status = (TextView) convertView.findViewById(R.id.jobJobStatus);
        	CupsPrintJobAttributes record = records.get(position);
        	
        	String jobId = String.valueOf(record.getJobID());
        	id.setText(jobId);
        	name.setText(record.getJobName());
        	status.setText(record.getJobState().getText());
        }catch (Exception e){
        	System.err.println(e.toString());
        }
        
        return convertView;
	}

}
