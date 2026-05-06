package fr.mnist.weka;

import java.io.*;
import java.util.*;

/**
 * Convertit les fichiers binaires MNIST (train-images.idx3-ubyte, train-labels.idx1-ubyte)
 * en fichiers ARFF (mnist_train.arff, mnist_test.arff) utilisables par Weka.
 */
public class MnistToArff {

    // Classe interne pour stocker une image MNIST avec son label
    static class MnistImage {
        int label;
        int[] pixels; // 784 valeurs (0-255)

        public MnistImage(int[] pixels, int label) {
            this.pixels = pixels.clone();
            this.label = label;
        }
    }

    // Charge toutes les images depuis les fichiers binaires
    public static List<MnistImage> loadMNIST(String imagePath, String labelPath) throws IOException {
        try (DataInputStream imgIn = new DataInputStream(new FileInputStream(imagePath));
             DataInputStream lblIn = new DataInputStream(new FileInputStream(labelPath))) {

            int magicImg = imgIn.readInt();
            int magicLbl = lblIn.readInt();
            if (magicImg != 0x00000803 || magicLbl != 0x00000801)
                throw new IOException("Magic number invalide");

            int numImages = imgIn.readInt();
            int rows = imgIn.readInt();
            int cols = imgIn.readInt();
            int numLabels = lblIn.readInt();
            if (numImages != numLabels)
                throw new IOException("Nombre d'images != nombre d'étiquettes");

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
                dataset.add(new MnistImage(pixels, label));
            }
            return dataset;
        }
    }

    // Sauvegarde une liste d'images au format ARFF
    public static void saveAsArff(String filePath, List<MnistImage> dataset) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("@RELATION mnist_all_digits\n\n");
            // 784 attributs pour les pixels
            for (int i = 1; i <= 784; i++) {
                writer.append("@ATTRIBUTE pixel").append(String.valueOf(i)).append(" NUMERIC\n");
            }
            // Attribut classe (0-9)
            writer.append("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9}\n\n");
            writer.append("@DATA\n");

            for (MnistImage img : dataset) {
                for (int p : img.pixels) {
                    writer.append(String.valueOf(p)).append(",");
                }
                writer.append(String.valueOf(img.label)).append("\n");
            }
        }
    }

    // Génère les fichiers ARFF à partir des fichiers binaires (en divisant train/test)
    public static void generateArffs() throws Exception {
        System.out.println("Chargement des données MNIST (60000 images)...");
        List<MnistImage> all = loadMNIST("train-images.idx3-ubyte", "train-labels.idx1-ubyte");
        System.out.println("Total : " + all.size() + " images");

        // Mélanger pour une répartition aléatoire
        Collections.shuffle(all, new Random(42));
        int trainSize = 48000; // 48k train, 12k test
        List<MnistImage> trainSet = all.subList(0, trainSize);
        List<MnistImage> testSet  = all.subList(trainSize, all.size());

        System.out.println("Création de mnist_train.arff (" + trainSet.size() + " images)...");
        saveAsArff("mnist_train.arff", trainSet);
        System.out.println("Création de mnist_test.arff (" + testSet.size() + " images)...");
        saveAsArff("mnist_test.arff", testSet);
        System.out.println("Terminé.");
    }

    public static void main(String[] args) throws Exception {
        generateArffs();
    }
}