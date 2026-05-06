package fr.mnist.ml;

import fr.mnist.data.PixelVector;
import fr.mnist.exceptions.ModelNotFoundException;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class RandomForestModel implements Classifier {
    private RandomForest model;
    private Instances structure;
    private boolean isTrained = false;
    private double accuracy = 0;

    @Override
    public void train(String arffPath) throws Exception {
        weka.core.converters.ConverterUtils.DataSource source =
            new weka.core.converters.ConverterUtils.DataSource(arffPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        model = new RandomForest();
        model.buildClassifier(data);
        structure = new Instances(data, 0);
        isTrained = true;
    }

    @Override
    public int predict(PixelVector v) throws ModelNotFoundException {
        if (!isTrained) throw new ModelNotFoundException(
            "RandomForest non entraîné.");
        try {
            Instance inst = new DenseInstance(1.0, v.normalize());
            inst.setDataset(structure);
            double pred = model.classifyInstance(inst);
            return (pred == 0.0) ? 3 : 5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getAccuracy() { return accuracy; }

    @Override
    public String getModelName() { return "Random Forest"; }

    public void evaluateCrossValidation(String arffPath) throws Exception {
        weka.core.converters.ConverterUtils.DataSource source =
            new weka.core.converters.ConverterUtils.DataSource(arffPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        weka.classifiers.evaluation.Evaluation eval =
            new weka.classifiers.evaluation.Evaluation(data);
        eval.crossValidateModel(new RandomForest(), data, 10, new java.util.Random(1));
        accuracy = eval.pctCorrect();
    }
}
