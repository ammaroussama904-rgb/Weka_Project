package fr.mnist.data;

import fr.mnist.exceptions.MnistIOException;
import fr.mnist.exceptions.DataFormatException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Source de données à partir d'un fichier texte CSV.
 * Chaque ligne contient 784 entiers (pixel1..pixel784) suivis du label (0-9), séparés par des virgules.
 */
public class TextDataSource extends DataSource {

    // Classe image interne pour éviter toute dépendance
    public static class MnistImage {
        private final int[] pixels;
        private final int label;

        public MnistImage(int[] pixels, int label) {
            this.pixels = pixels.clone();
            this.label = label;
        }

        public int[] getPixels() { return pixels.clone(); }
        public int getLabel() { return label; }
    }

    public TextDataSource(String filePath) {
        super(filePath);
    }

    @Override
    public List<MnistImage> readData() throws MnistIOException, DataFormatException {
        List<MnistImage> images = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new MnistIOException("Fichier introuvable : " + filePath);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String firstLine = br.readLine();
            if (firstLine == null) {
                return images; // fichier vide
            }

            // Détection d'un éventuel en-tête
            boolean hasHeader = firstLine.toLowerCase().contains("pixel") ||
                                firstLine.toLowerCase().contains("label");
            int lineNumber = 1;
            if (!hasHeader) {
                // La première ligne contient déjà des données
                processLine(firstLine, lineNumber++, images);
            } else {
                // L'en-tête est présent, on passe à la ligne suivante
                lineNumber = 2;
            }

            String line;
            while ((line = br.readLine()) != null) {
                processLine(line, lineNumber++, images);
            }

        } catch (IOException e) {
            throw new MnistIOException("Erreur de lecture du fichier : " + filePath, e);
        }
        return images;
    }

    private void processLine(String line, int lineNumber, List<MnistImage> images) throws DataFormatException {
        String[] parts = line.split(",");
        if (parts.length != 785) {
            throw new DataFormatException("Ligne " + lineNumber + " : 785 valeurs attendues, trouvé " + parts.length);
        }
        int[] pixels = new int[784];
        try {
            for (int i = 0; i < 784; i++) {
                pixels[i] = Integer.parseInt(parts[i].trim());
                if (pixels[i] < 0 || pixels[i] > 255) {
                    throw new DataFormatException("Ligne " + lineNumber + " : pixel hors limite [0-255] : " + pixels[i]);
                }
            }
            int label = Integer.parseInt(parts[784].trim());
            if (label < 0 || label > 9) {
                throw new DataFormatException("Ligne " + lineNumber + " : label invalide (0-9) : " + label);
            }
            images.add(new MnistImage(pixels, label));
        } catch (NumberFormatException e) {
            throw new DataFormatException("Ligne " + lineNumber + " : valeur non numérique");
        }
    }
}