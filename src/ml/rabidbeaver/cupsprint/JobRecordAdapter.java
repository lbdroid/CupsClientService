package ml.rabidbeaver.cupsprint;

import ml.rabidbeaver.cupsprintservice.R;
import ml.rabidbeaver.jna.MlRabidbeaverJnaLibrary.ipp_jstate_e;
import ml.rabidbeaver.jna.cups_job_s;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JobRecordAdapter extends BaseAdapter{

	Activity activity;
	cups_job_s[] records;
	
	public JobRecordAdapter(Activity activity){
		this.activity = activity;
	}
	
	public void setRecords(cups_job_s[] jobList){
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
            
            //TODO: activity_job_list_row is far too short to be touch clickable.
            //  make it taller.
            convertView = inflater.inflate(R.layout.job_list_row, null);
        }
        try {
        	TextView id = (TextView) convertView.findViewById(R.id.jobJobId);
        	TextView name = (TextView) convertView.findViewById(R.id.jobJobName);
        	TextView status = (TextView) convertView.findViewById(R.id.jobJobStatus);
        	cups_job_s record = records[position];
        	
        	String jobId = String.valueOf(record.id);
        	id.setText(jobId);
        	name.setText(record.title.getString(0));
        	switch(record.state){
        	case ipp_jstate_e.IPP_JSTATE_PENDING:
        		status.setText("PENDING");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_HELD:
        		status.setText("HELD");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_PROCESSING:
        		status.setText("PROCESSING");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_STOPPED:
        		status.setText("STOPPED");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_CANCELED:
        		status.setText("CANCELED");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_ABORTED:
        		status.setText("ABORTED");
        		break;
        	case ipp_jstate_e.IPP_JSTATE_COMPLETED:
        		status.setText("COMPLETED");
        		break;
        	}
        }catch (Exception e){
        	System.err.println(e.toString());
        }
        
        return convertView;
	}

}
