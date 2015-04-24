package ml.rabidbeaver.cupscontrols;

import org.cups4j.ppd.PpdItemList;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;

public class IntegerEdit extends EditText{
	

	private PpdItemList section;
	
	public IntegerEdit(Context context){
		super(context);
	}
		
	public IntegerEdit(int id, Context context, PpdItemList section){
		super(context);
		setId(id);
		setText(section.getSavedValue());
 		setInputType(InputType.TYPE_CLASS_NUMBER);
	 	setTextSize(CupsControl.TextSize);
	 	setTextScaleX(CupsControl.TextScale);
	 	this.section = section;
	 }
	
	 public boolean validate(){
		String text = getText().toString();
	 	try {
	 		@SuppressWarnings("unused")
	 		int i = Integer.parseInt(text);
	 		return true;
	 		}
	 		catch (Exception e){
	 			AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
	 			builder.setMessage(section.getName() + " must be an integer")
	 			       .setTitle("error");
	 			AlertDialog dialog = builder.create();	
	 			dialog.show();
	 			this.requestFocus();
	 			return false;
	 		}
	 }
	 
	 public void update(){
		 section.setSavedValue(getText().toString());
	 }
	
}
