package parser;

import java.io.Serializable;


public class Options implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
	String trainFile = "example.train";
	String predFile = "example.pred";
	String unimapFile = null;
	String outFile = "example.out";
	String modelFile = "example.model";
	private String format = "CONLL-09";

	public String getFormat() {
		return format;
	}

	int numberOfPreTrainingIterations = 2;
	int numberOfTrainingIterations = 10;
	boolean initTensorWithPretrain = true;
	float regularization = 0.01f;
	float gammaLabel = 0;
	int rankFirstOrderTensor = 50;
	int rankSecondOrderTensor = 30;

    public Options() {

	}

	private Options(Options options) {
        this.trainFile = options.trainFile;
        this.predFile = options.predFile;
        this.unimapFile = options.unimapFile;
        this.outFile = options.outFile;
        this.modelFile = options.modelFile;
        this.format = options.format;

        this.numberOfPreTrainingIterations = options.numberOfPreTrainingIterations;
 		this.numberOfTrainingIterations = options.numberOfTrainingIterations;
		this.initTensorWithPretrain = options.initTensorWithPretrain;
		this.regularization = options.regularization;
		this.gammaLabel = options.gammaLabel;
		this.rankFirstOrderTensor = options.rankFirstOrderTensor;
		this.rankSecondOrderTensor = options.rankSecondOrderTensor;
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
        System.out.println("iters: " + numberOfTrainingIterations);
        System.out.println("gammaLabel: " + gammaLabel);
        System.out.println("Regularization: " + regularization);
        System.out.println("rankFirstOrderTensor: " + rankFirstOrderTensor);
        System.out.println("rankSecondOrderTensor: " + rankSecondOrderTensor);
        System.out.println("file format: " + format);

        System.out.println();
    	System.out.println("------\n");
    }
    
}

