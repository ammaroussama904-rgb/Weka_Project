package fr.mnist.weka;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaClassifier {
    public static void main(String[] args) throws Exception {
        DataSource trainSource = new DataSource("mnist_train.arff");
        Instances trainData = trainSource.getDataSet();
        trainData.setClassIndex(trainData.numAttributes() - 1);

        DataSource testSource = new DataSource("mnist_test.arff");
        Instances testData = testSource.getDataSet();
        testData.setClassIndex(testData.numAttributes() - 1);

        System.out.println("Train instances : " + trainData.numInstances());
        System.out.println("Test instances  : " + testData.numInstances());

        RandomForest rf = new RandomForest();  // 100 arbres par défaut
        System.out.println("Construction du modèle RandomForest...");
        rf.buildClassifier(trainData);
        SerializationHelper.write("mnist_rf.model", rf);

        Evaluation eval = new Evaluation(trainData);
        eval.evaluateModel(rf, testData);
        System.out.println(eval.toSummaryString());
        System.out.printf("Précision : %.2f%%\n", eval.pctCorrect());
        System.out.println(eval.toMatrixString());
    }
}