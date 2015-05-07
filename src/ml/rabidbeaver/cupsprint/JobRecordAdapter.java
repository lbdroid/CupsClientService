package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.cupsjni.cups_job_s;
import ml.rabidbeaver.cupsprintservice.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JobRecordAdapter extends BaseAdapter{

	Activity activity;
	cups_job_s.ByReference[] records;
	
	public JobRecordAdapter(Activity activity){
		this.activity = activity;
	}
	
	public void setRecords(cups_job_s.ByReference[] jobList){
		this.records = jobList;
	}
	
	@Override
	public int getCount() {
		if (records == null) return 0;
		return records.length;
	}

	@Override
	public Object getItem(int position) {
		return records[position];
	}

	@Override
	public long getItemId(int position) {
		return records[position].id;
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
        	cups_job_s.ByReference record = records[position];
        	
        	String jobId = String.valueOf(record.id);
        	id.setText(jobId);
        	name.setText(record.toString());
        	//TODO ??? status.setText(record.getJobState().getText());
        	status.setText(record.state);
        }catch (Exception e){
        	System.err.println(e.toString());
        }
        
        return convertView;
	}

}
