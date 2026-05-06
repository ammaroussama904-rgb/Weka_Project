package fr.mnist.gui;

import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MnistGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextArea logArea;
    private JLabel accuracyLabel;

    public MnistGUI() {
        setTitle("Classifieur MNIST - Évaluation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        JButton evalBtn = new JButton("Lancer l'évaluation (k-NN)");
        accuracyLabel = new JLabel("Précision : --");
        bottom.add(evalBtn);
        bottom.add(accuracyLabel);
        add(bottom, BorderLayout.SOUTH);
        evalBtn.addActionListener(e -> runEvaluation());
    }

    private void runEvaluation() {
        try {
            File train = new File("mnist_train.arff");
            File test = new File("mnist_test.arff");
            if (!train.exists() || !test.exists()) {
                log("Fichiers ARFF introuvables. Générez-les d'abord.");
                return;
            }
            DataSource trainSrc = new DataSource("mnist_train.arff");
            Instances trainData = trainSrc.getDataSet();
            trainData.setClassIndex(trainData.numAttributes() - 1);
            DataSource testSrc = new DataSource("mnist_test.arff");
            Instances testData = testSrc.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);

            IBk knn = new IBk(3);
            log("Construction du modèle k-NN...");
            long start = System.currentTimeMillis();
            knn.buildClassifier(trainData);
            long end = System.currentTimeMillis();
            log("Modèle construit en " + (end - start) + " ms");

            Evaluation eval = new Evaluation(trainData);
            eval.evaluateModel(knn, testData);
            double acc = eval.pctCorrect();
            accuracyLabel.setText(String.format("Précision : %.2f%%", acc));
            log(eval.toSummaryString());
            log("Matrice de confusion :\n" + eval.toMatrixString());
        } catch (Exception ex) {
            log("Erreur : " + ex.getMessage());
        }
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MnistGUI().setVisible(true));
    }
}