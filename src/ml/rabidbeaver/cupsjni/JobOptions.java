package ml.rabidbeaver.cupsjni;

public class JobOptions {
	public String name;
	public String value;
	public boolean defopt = false;
	public JobOptions (){}
	public JobOptions (String name, String value){
		this.name=name;
		this.value=value;
	}
	public String toString(){
		return value;
	}
}
