package parser;

import java.io.IOException;

public class TypedDependencyParser {

    private Options options = new Options();

    public static void main(String[] args) throws IOException, CloneNotSupportedException, ClassNotFoundException

    {
        TypedDependencyParser typedDependencyParser = new TypedDependencyParser();

        typedDependencyParser.trainDependencyParser();

        typedDependencyParser.predictDependencyParser();

    }

    private void trainDependencyParser() throws IOException, CloneNotSupportedException {
        DependencyParser dependencyParser = new DependencyParser();
        dependencyParser.setOptions(options);
        options.printOptions();

        DependencyPipe pipe = new DependencyPipe(options);
        dependencyParser.setPipe(pipe);

        pipe.createAlphabets(options.trainFile);

        DependencyInstance[] dependencyInstances = pipe.createInstances(options.trainFile);
        pipe.pruneLabel(dependencyInstances);

        dependencyParser.setParameters(new Parameters(pipe, options));
        dependencyParser.train(dependencyInstances);
        dependencyParser.saveModel();
    }

    private void predictDependencyParser() throws IOException, ClassNotFoundException{
        DependencyParser dependencyParser = new DependencyParser();
        dependencyParser.setOptions(options);
        dependencyParser.loadModel();
        dependencyParser.predictDependency();
    }

}
