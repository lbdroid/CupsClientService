package ml.rabidbeaver.cupscontrols;

public class IntegerControl extends CupsControl<IntegerEdit>{
	
	public IntegerControl(IntegerEdit control){
		super(control);
	}

	public boolean validate(){
		return control.validate();
	}
	
	public void update(){
		control.update();
	}
	

}
