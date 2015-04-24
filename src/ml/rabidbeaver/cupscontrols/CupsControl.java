package ml.rabidbeaver.cupscontrols;

import android.view.View;

public abstract class CupsControl <T extends View> {

	public static int TextSize = 14;
	public static float TextScale = 0.8f;
	protected T control;

	public CupsControl(T control){
		this.control = control;
	}

	public abstract boolean validate();
	
	public abstract void update();

}
