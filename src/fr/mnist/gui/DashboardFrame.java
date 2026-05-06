package fr.mnist.gui;

import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class DashboardFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private DrawingPanel drawingPanel;
    private JLabel resultLabel;
    private Classifier classifier;
    private Instances dataHeader;

    public DashboardFrame(Classifier classifier) throws Exception {
        this.classifier = classifier;
        // Charger l'en-tête ARFF pour créer des instances
        DataSource ds = new DataSource("mnist_train.arff");
        dataHeader = ds.getDataSet();
        dataHeader.setClassIndex(dataHeader.numAttributes() - 1);

        setTitle("Reconnaissance MNIST - Tableau de bord");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(28, 28, 10);
        add(drawingPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnClear = new JButton("Effacer");
        JButton btnPredict = new JButton("Prédire");
        resultLabel = new JLabel("Résultat : --");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(btnClear);
        bottomPanel.add(btnPredict);
        bottomPanel.add(resultLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        btnClear.addActionListener(e -> drawingPanel.clear());
        btnPredict.addActionListener(e -> predict());
    }

    private void predict() {
        if (classifier == null || dataHeader == null) {
            resultLabel.setText("Résultat : modèle non disponible");
            return;
        }
        try {
            double[] rawPixels = drawingPanel.getPixelValues();
            DenseInstance instance = new DenseInstance(784);
            for (int i = 0; i < 784; i++) instance.setValue(i, rawPixels[i] * 255.0);
            instance.setDataset(dataHeader);
            double pred = classifier.classifyInstance(instance);
            int digit = (int) pred;
            double[] dist = classifier.distributionForInstance(instance);
            double confidence = dist[digit] * 100;
            resultLabel.setText(String.format("Résultat : %d  (confiance : %.2f%%)", digit, confidence));
        } catch (Exception e) {
            resultLabel.setText("Résultat : erreur");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        File modelFile = new File("mnist_knn.model");
        if (!modelFile.exists()) {
            System.err.println("Modèle mnist_knn.model introuvable. Lancez d'abord WekaClassifier.");
            return;
        }
        Classifier knn = (Classifier) SerializationHelper.read(modelFile.getAbsolutePath());
        DashboardFrame frame = new DashboardFrame(knn);
        frame.setVisible(true);
    }
}