package parser;

import java.io.Serializable;


public class Options implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
		
	String trainFile = "example.train";
	String predFile = "example.pred";
	String unimapFile = null;
	String outFile = "example.out";
	boolean train = true;
	boolean test = true;
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

	boolean average = true;
	float C = 0.01f;
	float gammaLabel = 0;
	int R = 50;
	int R2 = 30;
	
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
	
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		return super.clone();
	}
    
    public void printOptions() {
    	System.out.println("------\nFLAGS\n------");
    	System.out.println("train-file: " + trainFile);
    	System.out.println("pred-file: " + predFile);
    	System.out.println("model-name: " + modelFile);
        System.out.println("output-file: " + outFile);
    	System.out.println("train: " + train);
    	System.out.println("test: " + test);
        System.out.println("iters: " + maxNumIters);
        System.out.println("max-sent: " + maxNumSent);   
        System.out.println("gammaLabel: " + gammaLabel);
        System.out.println("C: " + C);
        System.out.println("R: " + R);
        System.out.println("R2: " + R2);
        System.out.println("word-vector:" + wordVectorFile);
        System.out.println("file format: " + format);
        System.out.println("feature hash bits: " + bits);
        
        System.out.println();
        System.out.println("use grandparent: " + useGP);

    	System.out.println("------\n");
    }
    
}

