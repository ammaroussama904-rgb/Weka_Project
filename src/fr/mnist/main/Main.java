package fr.mnist.main;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;



/**
 * Point d'entrée principal du projet.
 * Génère les fichiers ARFF, entraîne un modèle Naive Bayes et lance l'interface.
 */
public class Main {

    public static void main(String[] args) {
        try {
            // 1. Générer les fichiers ARFF à partir des fichiers binaires MNIST
            System.out.println("Génération des fichiers ARFF...");
            fr.mnist.weka.MnistToArff.main(new String[0]);
            System.out.println("Fichiers ARFF créés : mnist_train.arff et mnist_test.arff");

            // 2. Charger les données d'entraînement
            DataSource trainSource = new DataSource("mnist_train.arff");
            Instances trainData = trainSource.getDataSet();
            trainData.setClassIndex(trainData.numAttributes() - 1);

            // 3. Charger les données de test
            DataSource testSource = new DataSource("mnist_test.arff");
            Instances testData = testSource.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);

            // 4. Entraîner Naive Bayes avec Weka
            NaiveBayes nb = new NaiveBayes();
            System.out.println("Entraînement du modèle Naive Bayes...");
            nb.buildClassifier(trainData);

            // 5. Évaluer sur le jeu de test
            Evaluation eval = new Evaluation(trainData);
            eval.evaluateModel(nb, testData);
            double accuracy = eval.pctCorrect();

            System.out.printf("Précision sur le test : %.2f%%\n", accuracy);
            System.out.println(eval.toSummaryString());

            // 6. (Optionnel) Lancer une interface graphique, par exemple MnistGUI
            // SwingUtilities.invokeLater(() -> new fr.mnist.gui.MnistGUI().setVisible(true));

        } catch (Exception e) {
            System.err.println("Erreur fatale : " + e.getMessage());
            e.printStackTrace();
        }
    }
}