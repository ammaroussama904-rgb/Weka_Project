package fr.mnist.ml;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Construit un fichier ARFF à partir des fichiers binaires MNIST (images et labels).
 * Version autonome sans dépendance externe.
 */
public class ArffBuilder {

    // Classe interne pour une image MNIST (simplifiée)
    static class MnistImage {
        private final int label;
        private final int[] pixels; // 784 valeurs 0-255

        public MnistImage(int label, int[] pixels) {
            this.label = label;
            this.pixels = pixels.clone();
        }
        public int getLabel() { return label; }
        public int[] getPixels() { return pixels.clone(); }
    }

    // Lecture des fichiers binaires MNIST
    public static List<MnistImage> loadMNIST(String imagePath, String labelPath) throws Exception {
        try (DataInputStream imgIn = new DataInputStream(new FileInputStream(imagePath));
             DataInputStream lblIn = new DataInputStream(new FileInputStream(labelPath))) {

            int magicImg = imgIn.readInt();
            int magicLbl = lblIn.readInt();
            if (magicImg != 0x00000803 || magicLbl != 0x00000801)
                throw new Exception("Magic number invalide");

            int numImages = imgIn.readInt();
            int rows = imgIn.readInt();
            int cols = imgIn.readInt();
            int numLabels = lblIn.readInt();
            if (numImages != numLabels) throw new Exception("Nombre d'images différent");

            List<MnistImage> images = new ArrayList<>();
            int imageSize = rows * cols;

            for (int i = 0; i < numImages; i++) {
                byte[] buffer = new byte[imageSize];
                imgIn.readFully(buffer);
                int[] pixels = new int[imageSize];
                for (int j = 0; j < imageSize; j++) {
                    pixels[j] = buffer[j] & 0xFF;
                }
                int label = lblIn.readUnsignedByte();
                images.add(new MnistImage(label, pixels));
            }
            return images;
        }
    }

    // Génère un fichier ARFF binaire (3 et 5 seulement)
    public static void buildBinaryArff(String arffPath) throws Exception {
        // Charger toutes les images (60000)
        List<MnistImage> all = loadMNIST("train-images.idx3-ubyte", "train-labels.idx1-ubyte");
        System.out.println("Total chargé : " + all.size());

        // Filtrer les chiffres 3 et 5
        List<MnistImage> filtered = new ArrayList<>();
        for (MnistImage img : all) {
            int label = img.getLabel();
            if (label == 3 || label == 5) filtered.add(img);
        }
        System.out.println("Images 3 et 5 : " + filtered.size());

        // Attributs Weka
        ArrayList<Attribute> atts = new ArrayList<>();
        for (int i = 1; i <= 784; i++) atts.add(new Attribute("pixel_" + i));
        ArrayList<String> classVals = new ArrayList<>();
        classVals.add("trois");
        classVals.add("cinq");
        atts.add(new Attribute("class", classVals));

        Instances dataset = new Instances("MNIST_3_5", atts, filtered.size());
        dataset.setClassIndex(784);

        // Remplir les instances
        for (MnistImage img : filtered) {
            double[] vals = new double[785];
            int[] pix = img.getPixels();
            for (int j = 0; j < 784; j++) {
                vals[j] = pix[j] / 255.0; // normalisation
            }
            vals[784] = (img.getLabel() == 3) ? 0.0 : 1.0;
            dataset.add(new DenseInstance(1.0, vals));
        }

        // Sauvegarde ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File(arffPath));
        saver.writeBatch();
        System.out.println("ARFF créé : " + arffPath);
    }

    public static void main(String[] args) throws Exception {
        buildBinaryArff("mnist_3_5.arff");
    }
}