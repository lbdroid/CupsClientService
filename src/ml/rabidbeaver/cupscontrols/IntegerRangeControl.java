package ml.rabidbeaver.cupscontrols;

public class IntegerRangeControl extends CupsControl<IntegerRangeEdit>{

	public IntegerRangeControl(IntegerRangeEdit control){
		super(control);
	}

	public boolean validate(){
		return control.validate();
	}
	
	public void update(){
		control.update();
	}
	
}
