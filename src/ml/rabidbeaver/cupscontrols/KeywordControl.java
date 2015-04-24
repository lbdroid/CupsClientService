package ml.rabidbeaver.cupscontrols;

public class KeywordControl extends CupsControl<KeywordEdit> {

	public KeywordControl(KeywordEdit control){
		super(control);
	}

	public boolean validate(){
		return control.validate();
	}
	
	public void update(){
		control.update();
	}
	
}
