package ml.rabidbeaver.detect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Merger {

    public void merge(List<PrinterRec> httpRecs,List<PrinterRec>httpsRecs){
    	List<PrinterRec> tmpRecs = new ArrayList<PrinterRec>();
    	for (PrinterRec httpRec: httpRecs){
    		boolean match = false;
    		for (PrinterRec httpsRec: httpsRecs){
    			if (httpRec.getQueue().equals(httpsRec.getQueue()) &&
    					httpRec.getHost().equals(httpsRec.getHost()) &&
    					httpRec.getPort() == httpsRec.getPort()){
    				match = true;
    				break;
    			}
    		}
    		if (!match){
    			tmpRecs.add(httpRec);
    		}
    	}
    	for (PrinterRec rec: tmpRecs){
    		httpsRecs.add(rec);
    	}
    	Collections.sort(httpsRecs);
    }
}
