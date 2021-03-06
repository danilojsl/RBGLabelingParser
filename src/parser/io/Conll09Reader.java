package parser.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import parser.DependencyInstance;
import parser.Options;


public class Conll09Reader extends DependencyReader {

	Conll09Reader(Options options) {
		this.options = options;
	}
	
	@Override
	public DependencyInstance nextInstance(Map<String, String> coarseMap) throws IOException {
		
	    ArrayList<String[]> lstLines = new ArrayList<>();

	    String line = reader.readLine();
	    while (line != null && !line.equals("") && !line.startsWith("*")) {
	    	lstLines.add(line.trim().split("\t"));
	    	line = reader.readLine();
	    }
	    
	    if (lstLines.isEmpty()) return null;
	    
	    int length = lstLines.size();
	    String[] forms = new String[length + 1];
	    String[] lemmas = new String[length + 1];
	    String[] cpos = new String[length + 1];
	    String[] pos = new String[length + 1];
	    String[][] feats = new String[length + 1][];
	    String[] deprels = new String[length + 1];
	    int[] heads = new int[length + 1];
	    int[] predIndex = new int[length + 1];

	    forms[0] = "<root>";
	    lemmas[0] = "<root-LEMMA>";
	    pos[0] = "<root-POS>";
	    cpos[0] = coarseMap.get(pos[0]);
	    if (cpos[0] == null)
	    	cpos[0] = pos[0];
	    deprels[0] = "<no-type>";
	    heads[0] = -1;
	    for (int i = 0; i < predIndex.length; ++i) {
	    	predIndex[i] = -1;
	    }
	    
	    boolean hasLemma = false;
	    
	    
	    /*
	     *  CoNLL 2009 format:
		    0 ID
		    1 FORM
		    2 LEMMA (not used)
		    3 PLEMMA 
		    4 POS (not used)
		    5 PPOS   
		    6 FEAT (not used)
		    7 PFEAT  
		    8 HEAD
		    9 PHEAD
		    10 DEPREL 
		    11 PDEPREL 
		    12 FILLPRED 
		    13 PRED
		    14... APREDn
	   	*/	    
	    // 11  points  point   point   NNS NNS _   _   8   8   PMOD    PMOD    Y   point.02    _   _   _   _	    
	    // 1   杩�  杩�  杩�  DT  DT  _   _   6   4   DMOD    ADV _   _   _   _   _   _
	    
	    for (int i = 1; i < length + 1; ++i) {
	    	String[] parts = lstLines.get(i-1);
	    	forms[i] = parts[1];
	    	if (!parts[3].equals("_")) { 
	    		lemmas[i] = parts[3];
	    		hasLemma = true;
	    	}
	    	
	    	pos[i] = parts[5];
	    	cpos[i] = coarseMap.get(pos[i]);
	    	if (cpos[i] == null)
		    	cpos[i] = pos[i];
	    	
	    	if (!parts[7].equals("_")) feats[i] = parts[7].split("\\|");
	    	heads[i] = Integer.parseInt(parts[8]);
	    	deprels[i] = parts[10];

	    }
	    if (!hasLemma) lemmas = null;
	    
		return new DependencyInstance(forms, lemmas, cpos, pos, feats, heads, deprels);
	}

}

