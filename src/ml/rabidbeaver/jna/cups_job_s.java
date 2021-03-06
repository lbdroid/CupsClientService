package ml.rabidbeaver.jna;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : /home/adam/git/CupsClientService/jni/cups-2.0.2/cups/cups.h:74</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class cups_job_s extends Structure {
	/** The job ID */
	public int id;
	/**
	 * Printer or class name<br>
	 * C type : char*
	 */
	public Pointer dest;
	/**
	 * Title/job name<br>
	 * C type : char*
	 */
	public Pointer title;
	/**
	 * User the submitted the job<br>
	 * C type : char*
	 */
	public Pointer user;
	/**
	 * Document format<br>
	 * C type : char*
	 */
	public Pointer format;
	/**
	 * Job state<br>
	 * C type : ipp_jstate_t
	 */
	public int state;
	/** Size in kilobytes */
	public int size;
	/** Priority (1-100) */
	public int priority;
	/** Time the job was completed */
	public NativeLong completed_time;
	/** Time the job was created */
	public NativeLong creation_time;
	/** Time the job was processed */
	public NativeLong processing_time;
	public cups_job_s() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("id", "dest", "title", "user", "format", "state", "size", "priority", "completed_time", "creation_time", "processing_time");
	}
	public cups_job_s(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends cups_job_s implements Structure.ByReference {
		
	};
	public static class ByValue extends cups_job_s implements Structure.ByValue {
		
	}
}
