package ml.rabidbeaver.cupscontrols;

public class EnumControl extends CupsControl<EnumEdit> {

	public EnumControl(EnumEdit control){
		super(control);
	}

	public boolean validate(){
		return control.validate();
	}
	
	public void update(){
		control.update();
	}
	
}
