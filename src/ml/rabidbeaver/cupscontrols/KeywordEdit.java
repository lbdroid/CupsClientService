package ml.rabidbeaver.cupscontrols;

import ml.rabidbeaver.cupsjni.PpdItem;
import ml.rabidbeaver.cupsjni.PpdItemList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class KeywordEdit extends Spinner{

	private PpdItemList section;
	
	public KeywordEdit(Context context){
		super(context);
	}
	
	public KeywordEdit(int id, Context context, int resId, PpdItemList section){
		super(context);
	 	this.section = section;
	 	setId(id);
	 	ArrayAdapter <PpdItem> aa = 
				new ArrayAdapter<PpdItem>(getContext(), resId, section);
	 			//new ArrayAdapter<PpdItem>(getContext(),android.R.layout.simple_spinner_item, section);
		setAdapter(aa);
		int size = section.size();
		for (int i=0; i<size; i++){
			if (section.get(i).getValue().equals(section.getSavedValue())){
				this.setSelection(i);
				break;
			}
		
		}
	 }
	
	 public boolean validate(){
		 return true;
	 }
	 
	 public void update(){
	 	PpdItem item = (PpdItem) this.getSelectedItem();
	 	section.setSavedValue(item.getValue());
	 }
}
