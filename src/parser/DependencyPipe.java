package parser;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import parser.feature.SyntacticFeatureFactory;
import parser.io.DependencyReader;
import utils.Dictionary;
import utils.DictionarySet;
import utils.Utils;
import static parser.feature.FeatureTemplate.Arc.*;
import static parser.feature.FeatureTemplate.Word.*;
import static utils.DictionarySet.DictionaryTypes.*;

public class DependencyPipe implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private Options options;
    private DictionarySet dictionaries;
    private SyntacticFeatureFactory synFactory;

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    SyntacticFeatureFactory getSynFactory() {
        return synFactory;
    }

    private double[] unknownWv = null;
		
    private String[] types;					// array that maps label index to label string

    public String[] getTypes() {
        return types;
    }

    // language specific info
	private HashSet<String> conjWord;
	private HashMap<String, String> coarseMap;
	
	// headPOS x modPOS x Label
	private boolean[][][] pruneLabel;

    boolean[][][] getPruneLabel() {
        return pruneLabel;
    }

    private int numCPOS;
	
	DependencyPipe(Options options)
	{
		dictionaries = new DictionarySet();
		synFactory = new SyntacticFeatureFactory(options);
		
		this.options = options;
				
		loadLanguageInfo();
	}
	
	/***
	 * load language specific information
	 * conjWord: word considered as a conjunction
	 * coarseMap: fine-to-coarse map 
	 */
	private void loadLanguageInfo() {
		// load coarse map
        coarseMap = new HashMap<>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(options.unimapFile))) {
                String str;
                while ((str = br.readLine()) != null) {
                    String[] data = str.split("\\s+");
                    coarseMap.put(data[0], data[1]);
                }
            }

            coarseMap.put("<root-POS>", "ROOT");
        } catch (Exception e) {
            System.out.println("Warning: couldn't find coarse POS map for this language");
        }

		// fill conj word
		conjWord = new HashSet<>();
		conjWord.add("and");
		conjWord.add("or");

	}

	/***F
	 * Build dictionaries that maps word strings, POS strings, etc into
	 * corresponding integer IDs. This method is called before creating 
	 * the feature alphabets and before training a dependency model. 
	 * 
	 * @param file file path of the training data
	 */
	private void createDictionaries(String file) throws IOException
	{
		
		long start = System.currentTimeMillis();
		System.out.println("Creating dictionaries ... ");

        dictionaries.setCounters();
        
		DependencyReader reader = DependencyReader.createDependencyReader(options);
		reader.startReading(file);
		DependencyInstance dependencyInstance = reader.nextInstance(coarseMap);
		
		int cnt = 0;
		while (dependencyInstance != null) {
			dependencyInstance.setInstIds(dictionaries, coarseMap, conjWord);
			
			dependencyInstance = reader.nextInstance(coarseMap);
			++cnt;
			if (options.maxNumSent != -1 && cnt >= options.maxNumSent) break;
		}
		reader.close();
		
		dictionaries.closeCounters();

		synFactory.setTokenStart(dictionaries.lookupIndex(POS, "#TOKEN_START#"));
		synFactory.setTokenEnd(dictionaries.lookupIndex(POS, "#TOKEN_END#"));
		synFactory.setTokenMid(dictionaries.lookupIndex(POS, "#TOKEN_MID#"));

		dictionaries.stopGrowth(DEPLABEL);
		dictionaries.stopGrowth(POS);
		dictionaries.stopGrowth(WORD);
				
		synFactory.setWordNumBits(Utils.log2((long) dictionaries.size(WORD) + 1));
		synFactory.setTagNumBits(Utils.log2((long) dictionaries.size(POS) + 1));
		synFactory.setDepNumBits(Utils.log2((long) dictionaries.size(DEPLABEL) + 1));
		synFactory.setFlagBits(2*synFactory.getDepNumBits() + 4);
		
		types = new String[dictionaries.size(DEPLABEL)];	 
		Dictionary labelDict = dictionaries.get(DEPLABEL);
		Object[] keys = labelDict.toArray();
		for (Object key : keys) {
			int id = labelDict.lookupIndex(key);
			types[id - 1] = (String) key;
		}
		
		System.out.printf("%d %d%n", NUM_WORD_FEAT_BITS, NUM_ARC_FEAT_BITS);
		System.out.printf("Lexical items: %d (%d bits)%n", 
				dictionaries.size(WORD), synFactory.getWordNumBits());
		System.out.printf("Tag/label items: %d (%d bits)  %d (%d bits)%n", 
				dictionaries.size(POS), synFactory.getTagNumBits(),
				dictionaries.size(DEPLABEL), synFactory.getDepNumBits());
		System.out.printf("Flag Bits: %d%n", synFactory.getFlagBits());
		System.out.printf("Creation took [%d ms]%n", System.currentTimeMillis() - start);
	}


	/***
	 * Create feature alphabets, which maps 64-bit feature code into
	 * its integer index (starting from index 0). This method is called
	 * before training a dependency model.
	 * 
	 * @param file  file path of the training data
	 */
	public void createAlphabets(String file) throws IOException
	{
	
		createDictionaries(file);
		
		if (options.wordVectorFile != null)
			loadWordVectors(options.wordVectorFile);
		
		long start = System.currentTimeMillis();
		System.out.print("Creating Alphabet ... ");
		
		HashSet<String> posTagSet = new HashSet<>();
		HashSet<String> cposTagSet = new HashSet<>();
		DependencyReader reader = DependencyReader.createDependencyReader(options);
		reader.startReading(file);
		
		DependencyInstance dependencyInstance = reader.nextInstance(coarseMap);
		int cnt = 0;

		while(dependencyInstance != null) {
			
			for (int i = 0; i < dependencyInstance.getLength(); ++i) {
				if (dependencyInstance.getPostags() != null) posTagSet.add(dependencyInstance.getPostags()[i]);
				if (dependencyInstance.getCpostags() != null) cposTagSet.add(dependencyInstance.getCpostags()[i]);
			}
			
			dependencyInstance.setInstIds(dictionaries, coarseMap, conjWord);
			
		    synFactory.initFeatureAlphabets(dependencyInstance);
				
		    dependencyInstance = reader.nextInstance(coarseMap);
		    cnt++;
	        if (options.maxNumSent != -1 && cnt >= options.maxNumSent) break;
		}
				
		System.out.printf("[%d ms]%n", System.currentTimeMillis() - start);
		
		closeAlphabets();
		reader.close();
		
		synFactory.checkCollisions();
		System.out.printf("Num of CONLL fine POS tags: %d%n", posTagSet.size());
		System.out.printf("Num of CONLL coarse POS tags: %d%n", cposTagSet.size());
		System.out.printf("Num of labels: %d%n", types.length);
		System.out.printf("Num of Syntactic Features: %d %d%n", 
				synFactory.getNumWordFeats(), synFactory.getNumLabeledArcFeats());
		numCPOS = cposTagSet.size();
	}
	
	/***
	 * Load word vectors. The real-value word vectors are used as auxiliary
	 * features for the tensor scores.
	 * 
	 * @param file  file path of the word vector file.
	 */
	private void loadWordVectors(String file) throws IOException
	{
		
		System.out.println("Loading word vectors...");

		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            String line = in.readLine();
            while (line != null) {
                line = line.trim();
                String[] parts = line.split("[ \t]");
                String word = parts[0];
                dictionaries.lookupIndex(WORDVEC, word);
                line = in.readLine();
            }
        }

		dictionaries.stopGrowth(WORDVEC);

		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(file),StandardCharsets.UTF_8))){
            double[][] wordVectors = new double[dictionaries.size(WORDVEC) + 1][];
            int upperCases = 0;
            int cnt = 0;
            double sumL2 = 0;
            double minL2 = Double.POSITIVE_INFINITY;
            double maxL2 = 0;
            String line = in.readLine();
            while (line != null) {
                line = line.trim();
                String[] parts = line.split("[ \t]");

                String word = parts[0];
                upperCases += Character.isUpperCase(word.charAt(0)) ? 1 : 0;
                ++cnt;

                double s = 0;
                double [] v = new double[parts.length - 1];
                for (int i = 0; i < v.length; ++i) {
                    v[i] = Double.parseDouble(parts[i+1]);
                    s += v[i]*v[i];
                }
                s = Math.sqrt(s);
                sumL2 += s;
                minL2 = Math.min(minL2, s);
                maxL2 = Math.max(maxL2, s);

                String unknowWord = "*UNKNOWN*";
                if (word.equalsIgnoreCase(unknowWord))
                    unknownWv = v;
                else {
                    int wordId = dictionaries.lookupIndex(WORDVEC, word);
                    if (wordId > 0) wordVectors[wordId] = v;
                }

                line = in.readLine();
            }

            sumL2 /= cnt;

            synFactory.setUnknownWv(unknownWv);
            synFactory.setWordVectors(wordVectors);

            System.out.printf("Vector norm: Avg: %f  Min: %f  Max: %f%n",
                    sumL2, minL2, maxL2);
        }

	}
	
	/***
	 * Close alphabets so the feature set wouldn't grow.
	 */
    public void closeAlphabets()
    {
		synFactory.closeAlphabets();
    }
    
    
    public DependencyInstance[] createInstances(String file) throws IOException
    {
    	
    	long start = System.currentTimeMillis();
    	System.out.print("Creating instances ... ");
    	
    	DependencyReader reader = DependencyReader.createDependencyReader(options);
		reader.startReading(file);

		LinkedList<DependencyInstance> lt = new LinkedList<>();
		DependencyInstance dependencyInstance = reader.nextInstance(coarseMap);
						
		int cnt = 0;
		while(dependencyInstance != null) {
			
			dependencyInstance.setInstIds(dictionaries, coarseMap, conjWord);
			
			lt.add(new DependencyInstance(dependencyInstance));
			
			dependencyInstance = reader.nextInstance(coarseMap);
			cnt++;
			if (options.maxNumSent != -1 && cnt >= options.maxNumSent) break;
			if (cnt % 1000 == 0)
				System.out.printf("%d ", cnt);
		}
				
		reader.close();
		closeAlphabets();
				
		DependencyInstance[] insts = new DependencyInstance[lt.size()];
		int N = 0;
		for (DependencyInstance p : lt) {
			insts[N++] = p;
		}
		
		System.out.printf("%d [%d ms]%n", cnt, System.currentTimeMillis() - start);
	    
		return insts;
	}
    
    public DependencyInstance createInstance(DependencyReader reader) throws IOException
    {
    	
    	DependencyInstance inst = reader.nextInstance(coarseMap);
    	if (inst == null) return null;
    	
    	inst.setInstIds(dictionaries, coarseMap, conjWord);
	    
	    return inst;
    }
    
    
    public void pruneLabel(DependencyInstance[] lstTrain)
    {
		int numPOS = dictionaries.size(POS) + 1;
		int numLab = dictionaries.size(DEPLABEL) + 1;
		this.pruneLabel = new boolean [numPOS][numPOS][numLab];
		int num = 0;

		for (DependencyInstance inst : lstTrain) {
			int n = inst.getLength();
			for (int mod = 1; mod < n; ++mod) {
				int head = inst.getHeads()[mod];
				int lab = inst.getDeplbids()[mod];
				if (!this.pruneLabel[inst.getCpostagids()[head]][inst.getCpostagids()[mod]][lab]) {
					this.pruneLabel[inst.getCpostagids()[head]][inst.getCpostagids()[mod]][lab] = true;
					num++;
				}
			}
		}
		
		System.out.println("Prune label: " + num + "/" + numCPOS*numCPOS*numLab);
    }
    
	
}
