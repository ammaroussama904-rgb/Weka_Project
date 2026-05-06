package fr.mnist.data;

import fr.mnist.exceptions.MnistIOException;
import fr.mnist.exceptions.DataFormatException;
import fr.mnist.exceptions.InvalidMNISTFormatException;
import java.util.List;
import java.util.ArrayList;

public class BinaryDataSource extends DataSource {

    public BinaryDataSource(String filePath) {
        super(filePath); // appel au constructeur parent
    }

    @Override
    public List<MnistImage> readData() throws MnistIOException, DataFormatException, InvalidMNISTFormatException {
        // Implémentez la lecture binaire ici
        List<MnistImage> images = new ArrayList<>();
        // ... code de lecture ...
        return images;
    }

    @Override
    public void writeData(List<?> images) throws MnistIOException {
        // Implémentez l'écriture binaire si nécessaire
        throw new MnistIOException("writeData non implémenté pour BinaryDataSource");
    }
}