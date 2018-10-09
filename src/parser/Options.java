package parser;

import java.io.Serializable;


public class Options implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
	String trainFile = "example.train";
	String predFile = "example.pred";
	String unimapFile = null;
	String outFile = "example.out";
	String wordVectorFile = null;
	String modelFile = "example.model";
	private String format = "CONLL-09";

	public String getFormat() {
		return format;
	}

	int maxNumSent = -1;
    int numPretrainIters = 1;
	int maxNumIters = 10;
	boolean initTensorWithPretrain = true;
	float C = 0.01f;
	float gammaLabel = 0;
	int rank = 50;
	int rank2 = 30;
	
	// feature set
	private int bits = 30;
	private boolean useGP = true;		// use grandparent

    public int getBits() {
        return bits;
    }

    public boolean isUseGP() {
        return useGP;
    }

    public Options() {

	}

	private Options(Options options) {
        this.trainFile = options.trainFile;
        this.predFile = options.predFile;
        this.unimapFile = options.unimapFile;
        this.outFile = options.outFile;
        this.wordVectorFile = options.wordVectorFile;
        this.modelFile = options.modelFile;
        this.format = options.format;

        this.maxNumSent = options.maxNumSent;
		this.numPretrainIters = options.numPretrainIters;
		this.maxNumIters = options.maxNumIters;
		this.initTensorWithPretrain = options.initTensorWithPretrain;
		this.C = options.C;
		this.gammaLabel = options.gammaLabel;
		this.rank = options.rank;
		this.rank2 = options.rank2;

		this.bits = options.bits;
		this.useGP = options.useGP;
	}

	static Options newInstance(Options options){
        //Copy factory
    	return new Options(options);
	}

    void printOptions() {
    	System.out.println("------\nFLAGS\n------");
    	System.out.println("train-file: " + trainFile);
    	System.out.println("pred-file: " + predFile);
    	System.out.println("model-name: " + modelFile);
        System.out.println("output-file: " + outFile);
        System.out.println("iters: " + maxNumIters);
        System.out.println("max-sent: " + maxNumSent);   
        System.out.println("gammaLabel: " + gammaLabel);
        System.out.println("C: " + C);
        System.out.println("rank: " + rank);
        System.out.println("rank2: " + rank2);
        System.out.println("word-vector:" + wordVectorFile);
        System.out.println("file format: " + format);
        System.out.println("feature hash bits: " + bits);
        
        System.out.println();
        System.out.println("use grandparent: " + useGP);

    	System.out.println("------\n");
    }
    
}

