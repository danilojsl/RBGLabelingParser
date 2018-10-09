package parser;

import java.io.IOException;

public class TypedDependencyParser {

    public static void main(String[] args)
            throws IOException, ClassNotFoundException, CloneNotSupportedException
    {

        Options options = new Options();

        if (options.train) {
            DependencyParser dependencyParser = new DependencyParser();
            dependencyParser.setOptions(options);
            options.printOptions();

            DependencyPipe pipe = new DependencyPipe(options);
            dependencyParser.setPipe(pipe);

            pipe.createAlphabets(options.trainFile);

            DependencyInstance[] lstTrain = pipe.createInstances(options.trainFile);
            pipe.pruneLabel(lstTrain);

            dependencyParser.setParameters(new Parameters(pipe, options));

            dependencyParser.train(lstTrain);
            dependencyParser.saveModel();
        }

        if (options.test) {
            DependencyParser dependencyParser = new DependencyParser();
            dependencyParser.setOptions(options);
            dependencyParser.loadModel();
            dependencyParser.predictDependency();
        }

    }

}
