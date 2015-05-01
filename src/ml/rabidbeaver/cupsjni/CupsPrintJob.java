package ml.rabidbeaver.cupsjni;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class CupsPrintJob {
    private ByteArrayInputStream document;
    private String jobName;
    private Map<String, String> attributes;
    private String mime = "application/octet-stream";

    public CupsPrintJob(byte[] document, String jobName) {
        this.document = new ByteArrayInputStream(document);
        this.jobName = jobName;
    }

    public CupsPrintJob(InputStream document, String jobName) {
        this.document = (ByteArrayInputStream) document;
        this.jobName = jobName;
    }
    
 // convert InputStream to String
 	private String getStringFromInputStream(InputStream is) {
  
 		BufferedReader br = null;
 		StringBuilder sb = new StringBuilder();
  
 		String line;
 		try {
  
 			br = new BufferedReader(new InputStreamReader(is));
 			while ((line = br.readLine()) != null) {
 				sb.append(line);
 			}
  
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
  
 		return sb.toString();
  
 	}

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public InputStream getDocument() {
        return document;
    }

    public void setAttributes(Map<String, String> printJobAttributes) {
        this.attributes = printJobAttributes;
    }

    public String getJobName() {
        return jobName;
    }
    
    public void setMimeType(String mime){
    	this.mime=mime;
    }
    
    public String getMimeType(){
    	return mime;
    }
    
    public String getDocumentString(){
    	return getStringFromInputStream(document);
    }
}
