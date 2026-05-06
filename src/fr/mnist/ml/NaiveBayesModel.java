package fr.mnist.ml;

import fr.mnist.data.PixelVector;
import fr.mnist.exceptions.ModelNotFoundException;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class NaiveBayesModel implements Classifier {
    private NaiveBayes model;
    private Instances structure;
    private boolean isTrained = false;
    private double accuracy = 0;

    @Override
    public void train(String arffPath) throws Exception {
        weka.core.converters.ConverterUtils.DataSource source =
            new weka.core.converters.ConverterUtils.DataSource(arffPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        model = new NaiveBayes();
        model.buildClassifier(data);
        structure = new Instances(data, 0);
        isTrained = true;
    }

    @Override
    public int predict(PixelVector v) throws ModelNotFoundException {
        if (!isTrained) throw new ModelNotFoundException(
            "Le modèle NaiveBayes n'est pas encore entraîné.");
        try {
            double[] features = v.normalize();
            Instance inst = new DenseInstance(1.0, features);
            inst.setDataset(structure);
            double predIndex = model.classifyInstance(inst);
            return (predIndex == 0.0) ? 3 : 5;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de prédiction", e);
        }
    }

    @Override
    public double getAccuracy() { return accuracy; }

    @Override
    public String getModelName() { return "Naive Bayes"; }

    public void evaluateCrossValidation(String arffPath) throws Exception {
        weka.core.converters.ConverterUtils.DataSource source =
            new weka.core.converters.ConverterUtils.DataSource(arffPath);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        weka.classifiers.evaluation.Evaluation eval =
            new weka.classifiers.evaluation.Evaluation(data);
        eval.crossValidateModel(new NaiveBayes(), data, 10, new java.util.Random(1));
        accuracy = eval.pctCorrect();
    }
}
