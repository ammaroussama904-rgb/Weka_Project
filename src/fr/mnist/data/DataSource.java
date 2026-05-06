package fr.mnist.data;

import fr.mnist.exceptions.MnistIOException;
import fr.mnist.exceptions.DataFormatException;
import fr.mnist.exceptions.InvalidMNISTFormatException;
import java.util.List;

public abstract class DataSource {
    protected String filePath;

    public DataSource(String filePath) {
        this.filePath = filePath;
    }

    public abstract List<?> readData() throws MnistIOException, DataFormatException, InvalidMNISTFormatException;

    // Optionnel: writeData par défaut lève une exception
    public void writeData(List<?> images) throws MnistIOException {
        throw new MnistIOException("writeData non implémenté");
    }
}