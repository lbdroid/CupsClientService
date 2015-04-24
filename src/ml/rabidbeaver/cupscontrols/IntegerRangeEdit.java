package ml.rabidbeaver.cupscontrols;

import org.cups4j.ppd.PpdItemList;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

	public class IntegerRangeEdit extends EditText{
		

		private PpdItemList section;
			
		public IntegerRangeEdit(Context context){
			super(context);
		}
		public IntegerRangeEdit(int id, Context context, PpdItemList section){
			super(context);
	 		setId(id);
			setText(section.getSavedValue());
		 	setInputType(InputType.TYPE_CLASS_TEXT);
		 	setTextSize(CupsControl.TextSize);
		 	setTextScaleX(CupsControl.TextScale);
		 	this.section = section;
		 	
		 }
		
		 public boolean validate(){
		 		return true;
		 }
		
		 public void update(){
		 	section.setSavedValue(getText().toString());
		 }

}
