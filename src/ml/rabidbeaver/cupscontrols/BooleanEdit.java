package ml.rabidbeaver.cupscontrols;

import java.util.Locale;

import ml.rabidbeaver.cupsjni.PpdItemList;

import android.content.Context;
import android.widget.CheckBox;

public class BooleanEdit extends CheckBox{

	private PpdItemList section;
	
	public BooleanEdit(Context context){
		super(context);
	}
	
	public BooleanEdit(int id, Context context, PpdItemList section){
		super(context);
	 	setId(id);
		this.section = section;
	 	if (section.getSavedValue().toLowerCase(Locale.ENGLISH).equals("true"))
	 		this.setChecked(true);
	 }
	
	 public boolean validate(){
		 return true;
	 }
	
	 public void update(){

		 if (isChecked())
			 section.setSavedValue("true");
		 else 
			 section.setSavedValue("false");
	 }
}
