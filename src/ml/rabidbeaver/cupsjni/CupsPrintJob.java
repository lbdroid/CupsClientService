package ml.rabidbeaver.cupsjni;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CupsPrintJob {
    private byte[] document;
    private String jobName;
    private List<JobOptions> attributes;
    private long length;
    private String mime = "application/octet-stream"; // I think this doesn't actually matter.

    public CupsPrintJob(byte[] document, String jobName) {
        this.document = document;
        this.jobName = jobName;
        this.length = document.length;
    }

    public CupsPrintJob(InputStream document, String jobName) {
        try {
			this.document = readBytes(document);
		} catch (IOException e1) {}
        this.length=this.document.length;
        this.jobName = jobName;
    }
    
    private byte[] readBytes(InputStream inputStream) throws IOException {
    	// this dynamically extends to take the bytes you read
    	ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

    	// this is storage overwritten on each iteration with bytes
    	int bufferSize = 1024;
    	byte[] buffer = new byte[bufferSize];

    	// we need to know how may bytes were read to write them to the byteBuffer
    	int len = 0;
    	while ((len = inputStream.read(buffer)) != -1) {
    		byteBuffer.write(buffer, 0, len);
    	}

    	// and then we can return your byte array.
    	return byteBuffer.toByteArray();
    }

    public List<JobOptions> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<JobOptions> printJobAttributes) {
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
    
    public byte[] getBytes(){
    	return document;
    }
    
    public long getDocumentLength(){
    	return length;
    }
}
