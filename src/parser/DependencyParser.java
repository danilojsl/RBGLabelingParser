package parser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import parser.io.DependencyReader;
import parser.io.DependencyWriter;

class DependencyParser implements Serializable {

	private static final long serialVersionUID = 1L;

	private Options options;
	private DependencyPipe pipe;
	private Parameters parameters;

    DependencyPipe getPipe() {
        return pipe;
    }

    Parameters getParameters() {
        return parameters;
    }

    public Options getOptions() {
        return options;
    }

    void setPipe(DependencyPipe pipe) {
		this.pipe = pipe;
	}

	void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

    public void setOptions(Options options) {
        this.options = options;
    }

    void saveModel() throws IOException
    {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new GZIPOutputStream(new FileOutputStream(options.modelFile)))) {
            out.writeObject(pipe);
            out.writeObject(parameters);
            out.writeObject(options);
        }
    }

    void loadModel() throws IOException, ClassNotFoundException
    {
        try (ObjectInputStream in = new ObjectInputStream(
                new GZIPInputStream(new FileInputStream(options.modelFile)))) {
            pipe = (DependencyPipe) in.readObject();
            parameters = (Parameters) in.readObject();
        }
        pipe.closeAlphabets();
    }
	
    void train(DependencyInstance[] dependencyInstances)
    {
    	long start;
        long end;

    	if ((options.R > 0 || options.R2 > 0) && options.gammaLabel < 1 && options.initTensorWithPretrain) {

        	Options optionsBackup = Options.newInstance(options);
        	options.R = 0;
        	options.R2 = 0;
        	options.gammaLabel = 1.0f;
			optionsBackup.maxNumIters = options.numPretrainIters;
        	parameters.setRank(0);
        	parameters.setRank2(0);
        	parameters.setGammaL(1.0f);

    		System.out.printf("Pre-training:%n");

    		start = System.currentTimeMillis();

    		System.out.println("Running MIRA ... ");
    		trainIter(dependencyInstances);
    		System.out.println();
    		
    		options = optionsBackup;
    		parameters.setRank(options.R);
        	parameters.setRank2(options.R2);
        	parameters.setGammaL(options.gammaLabel);
    		
    		System.out.println("Init tensor ... ");
    		int n = parameters.getNumberWordFeatures();
    		int d = parameters.getDL();
        	LowRankTensor tensor = new LowRankTensor(new int[] {n, n, d}, options.R);
        	LowRankTensor tensor2 = new LowRankTensor(new int[] {n, n, n, d, d}, options.R2);
        	pipe.getSynFactory().fillParameters(tensor, tensor2, parameters);
        	
        	ArrayList<float[][]> param = new ArrayList<>();
        	param.add(parameters.getU());
        	param.add(parameters.getV());
        	param.add(parameters.getWL());
        	tensor.decompose(param);
        	if (options.isUseGP()) {
        		ArrayList<float[][]> param2 = new ArrayList<>();
        		param2.add(parameters.getU2());
        		param2.add(parameters.getV2());
        		param2.add(parameters.getW2());
            	param2.add(parameters.getX2L());
            	param2.add(parameters.getY2L());
            	tensor2.decompose(param2);
        	}
        	parameters.assignTotal();
        	parameters.printStat();
        	
            System.out.println();
    		end = System.currentTimeMillis();
            System.out.println();
            System.out.printf("Pre-training took %d ms.%n", end-start);    		
    		System.out.println();

        } else {
        	parameters.randomlyInit();
        }
        
		System.out.printf(" Training:%n");

		start = System.currentTimeMillis();

		System.out.println("Running MIRA ... ");
		trainIter(dependencyInstances);
		System.out.println();
		
		end = System.currentTimeMillis();
		
		System.out.printf("Training took %d ms.%n", end-start);    		
		System.out.println();
    }
    
    private void trainIter(DependencyInstance[] dependencyInstances)
    {
    	int printPeriod = 10000 < dependencyInstances.length ? dependencyInstances.length/10 : 1000;
    	
    	for (int iIter = 0; iIter < options.maxNumIters; ++iIter) {

    		long start;
    		double loss = 0;
    		int las = 0;
            int tot = 0;
    		start = System.currentTimeMillis();	
    		
    		for (int i = 0; i < dependencyInstances.length; ++i) {
    			
    			if ((i + 1) % printPeriod == 0) {
				System.out.printf("  %d (time=%ds)", (i+1),
					(System.currentTimeMillis()-start)/1000);
    			}

    			DependencyInstance dependencyInstance = dependencyInstances[i];
    			LocalFeatureData lfd = new LocalFeatureData(dependencyInstance, this);
    		    int n = dependencyInstance.getLength();
    		    int[] predDeps = dependencyInstance.getHeads();
    		    int[] predLabs = new int [n];
    		        		
        		lfd.predictLabels(predDeps, predLabs, true);
        		int la = evaluateLabelCorrect(dependencyInstance.getHeads(), dependencyInstance.getDeplbids(),
											  predDeps, predLabs);
    			if (la != n-1) {
    				loss += parameters.updateLabel(dependencyInstance, predDeps, predLabs, lfd,
    						iIter * dependencyInstances.length + i + 1);
    			}
        		las += la;
        		tot += n-1;
    		}

    		tot = tot == 0 ? 1 : tot;

    		System.out.printf("%n  Iter %d\tloss=%.4f\tlas=%.4f\t[%ds]%n", iIter+1,
    				loss, las/(tot +0.0),
    				(System.currentTimeMillis() - start)/1000);
    		System.out.println();
    		
    		parameters.printStat();
    	}

    }
    
    private int evaluateLabelCorrect(int[] actDeps, int[] actLabs, int[] predDeps, int[] predLabs)
    {
    	int nCorrect = 0;
    	for (int i = 1, N = actDeps.length; i < N; ++i) {
    		if (actDeps[i] == predDeps[i] && actLabs[i] == predLabs[i])
    			++nCorrect;
    	}    		  		
    	return nCorrect;
    }
    
    void predictDependency()
    		throws IOException {

        System.out.printf(" Predicting...%n");

        //Initialize parameters for prediction files
    	DependencyReader predictionReader = DependencyReader.createDependencyReader(options);
    	predictionReader.startReading(options.predFile);

    	DependencyWriter writer = null;
    	if (options.outFile != null) {
    		writer = DependencyWriter.createDependencyWriter(options, pipe);
    		writer.startWriting(options.outFile);
    	}

    	DependencyInstance predictionDependencyInstance = pipe.createInstance(predictionReader);
    	while (predictionDependencyInstance != null) {

    		LocalFeatureData localFeatureData = new LocalFeatureData(predictionDependencyInstance, this);
            
    		int numberOfTokensInSentence = predictionDependencyInstance.getLength();
    		int[] predictedHeads = predictionDependencyInstance.getHeads();
		    int[] predictedLabels = new int [numberOfTokensInSentence];
            localFeatureData.predictLabels(predictedHeads, predictedLabels, false);
            
    		if (writer != null) {
    			writer.writeInstance(predictionDependencyInstance, predictedHeads, predictedLabels);
    		}
    		predictionDependencyInstance = pipe.createInstance(predictionReader);
    	}

    	predictionReader.close();
    	if (writer != null) {
    	    writer.close();
        }

        System.out.printf(" Done%n");
    }
}
