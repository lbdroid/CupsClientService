package ml.rabidbeaver.cupscontrols;

import ml.rabidbeaver.cupsjni.PpdItem;
import ml.rabidbeaver.cupsjni.PpdItemList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class EnumEdit extends Spinner{

	private PpdItemList section;
	
	public EnumEdit(Context context){
		super(context);
	}
	
	public EnumEdit(int id, Context context, int resourceId, PpdItemList section){
		super(context);
	 	setId(id);
		this.section = section;
		ArrayAdapter <PpdItem> aa = 
				new ArrayAdapter<PpdItem>(getContext(), resourceId, section);
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
