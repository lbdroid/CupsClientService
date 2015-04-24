package ml.rabidbeaver.cupscontrols;

public class BooleanControl extends CupsControl<BooleanEdit> {

	public BooleanControl(BooleanEdit control){
		super(control);
	}

	public boolean validate(){
		return control.validate();
	}
	
	public void update(){
		control.update();
	}
	
}
