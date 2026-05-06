package fr.mnist.data;

import fr.mnist.exceptions.MnistIOException;
import fr.mnist.exceptions.DataFormatException;
import fr.mnist.exceptions.InvalidMNISTFormatException;
import java.util.List;
import java.util.ArrayList;

public class ExcelDataSource extends DataSource {

    public ExcelDataSource(String filePath) {
        super(filePath);
    }

    @Override
    public List<MnistImage> readData() throws MnistIOException, DataFormatException, InvalidMNISTFormatException {
        List<MnistImage> images = new ArrayList<>();
        // Implémentez la lecture Excel (par exemple avec Apache POI)
        return images;
    }

    @Override
    public void writeData(List<?> images) throws MnistIOException {
        throw new MnistIOException("writeData non implémenté pour ExcelDataSource");
    }
}