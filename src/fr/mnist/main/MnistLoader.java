package fr.mnist.main;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Chargeur complet des données MNIST (format IDX binaire).
 * Affiche un aperçu, puis réalise une classification k-NN simple.
 */
public class MnistLoader {

    // --- Classe interne pour stocker une image avec son label ---
    static class MnistImage {
        private final int label;
        private final int rows;
        private final int cols;
        private final int[] pixels;   // 0-255

        public MnistImage(int rows, int cols, int[] pixels, int label) {
            this.rows = rows;
            this.cols = cols;
            this.pixels = pixels.clone();
            this.label = label;
        }

        public int getLabel() { return label; }
        public int getRows() { return rows; }
        public int getCols() { return cols; }
        public int[] getPixels() { return pixels.clone(); }

        /** Convertit les pixels en vecteur normalisé (valeurs entre 0 et 1) */
        public double[] toNormalizedVector() {
            double[] vec = new double[pixels.length];
            for (int i = 0; i < pixels.length; i++) {
                vec[i] = pixels[i] / 255.0;
            }
            return vec;
        }

        /** Affiche l'image dans la console (ASCII art) */
        public void displayAscii() {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int val = pixels[i * cols + j];
                    System.out.print(val > 128 ? "██" : "  ");
                }
                System.out.println();
            }
            System.out.println("Label : " + label);
        }
    }

    // --- Lecture des fichiers binaires MNIST ---
    public static List<MnistImage> loadMNIST(String imagePath, String labelPath) throws IOException {
        try (DataInputStream imgIn = new DataInputStream(new FileInputStream(imagePath));
             DataInputStream lblIn = new DataInputStream(new FileInputStream(labelPath))) {

            int magicImg = imgIn.readInt();
            int magicLbl = lblIn.readInt();
            if (magicImg != 0x00000803)
                throw new IOException("Mauvais magic number pour les images : " + Integer.toHexString(magicImg));
            if (magicLbl != 0x00000801)
                throw new IOException("Mauvais magic number pour les labels : " + Integer.toHexString(magicLbl));

            int numImages = imgIn.readInt();
            int rows = imgIn.readInt();
            int cols = imgIn.readInt();
            int numLabels = lblIn.readInt();

            if (numImages != numLabels)
                throw new IOException("Désaccord : " + numImages + " images vs " + numLabels + " labels");

            List<MnistImage> dataset = new ArrayList<>(numImages);
            int imageSize = rows * cols;

            for (int i = 0; i < numImages; i++) {
                byte[] buffer = new byte[imageSize];
                imgIn.readFully(buffer);
                int[] pixels = new int[imageSize];
                for (int j = 0; j < imageSize; j++) {
                    pixels[j] = buffer[j] & 0xFF;
                }
                int label = lblIn.readUnsignedByte();
                dataset.add(new MnistImage(rows, cols, pixels, label));
            }
            return dataset;
        }
    }

    // --- k-NN : distance euclidienne ---
    private static double euclideanDistance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public static int knnPredict(double[] testVec, List<double[]> trainVectors, List<Integer> trainLabels, int k) {
        int n = trainVectors.size();
        double[] distances = new double[n];
        for (int i = 0; i < n; i++) {
            distances[i] = euclideanDistance(testVec, trainVectors.get(i));
        }

        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparingDouble(idx -> distances[idx]));

        Map<Integer, Integer> votes = new HashMap<>();
        for (int i = 0; i < k; i++) {
            int label = trainLabels.get(indices[i]);
            votes.put(label, votes.getOrDefault(label, 0) + 1);
        }

        return Collections.max(votes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static double evaluateKnn(List<MnistImage> trainSet, List<MnistImage> testSet, int k) {
        List<double[]> trainVecs = new ArrayList<>();
        List<Integer> trainLabels = new ArrayList<>();
        for (MnistImage img : trainSet) {
            trainVecs.add(img.toNormalizedVector());
            trainLabels.add(img.getLabel());
        }

        int correct = 0;
        for (MnistImage testImg : testSet) {
            double[] testVec = testImg.toNormalizedVector();
            int predicted = knnPredict(testVec, trainVecs, trainLabels, k);
            if (predicted == testImg.getLabel()) correct++;
        }
        return (double) correct / testSet.size() * 100.0;
    }

    // --- Point d'entrée ---
    public static void main(String[] args) {
        try {
            System.out.println("Répertoire de travail : " + System.getProperty("user.dir"));
            System.out.println("Chargement des images MNIST...");

            List<MnistImage> allImages = loadMNIST("train-images.idx3-ubyte", "train-labels.idx1-ubyte");
            System.out.println("Chargement réussi ! " + allImages.size() + " images chargées.\n");

            System.out.println("=== Première image ===");
            allImages.get(0).displayAscii();
            System.out.println();

            Collections.shuffle(allImages, new Random(42));
            int splitIndex = (int) (allImages.size() * 0.8);
            List<MnistImage> trainSet = allImages.subList(0, splitIndex);
            List<MnistImage> testSet = allImages.subList(splitIndex, allImages.size());

            System.out.println("Taille entraînement : " + trainSet.size());
            System.out.println("Taille test : " + testSet.size());

            int k = 3;
            System.out.println("\nÉvaluation du k-NN (k=" + k + ") sur le jeu de test...");
            long start = System.currentTimeMillis();
            double accuracy = evaluateKnn(trainSet, testSet, k);
            long end = System.currentTimeMillis();

            System.out.printf("Précision : %.2f%%\n", accuracy);
            System.out.println("Temps d'exécution : " + (end - start) + " ms");

        } catch (FileNotFoundException e) {
            System.err.println("Fichier introuvable ! Vérifiez que train-images.idx3-ubyte et train-labels.idx1-ubyte sont dans le répertoire de travail.");
            System.err.println("Chemin courant : " + System.getProperty("user.dir"));
        } catch (IOException e) {
            System.err.println("Erreur de lecture : " + e.getMessage());
            e.printStackTrace();
        }
    }
}