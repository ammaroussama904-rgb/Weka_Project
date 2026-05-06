package fr.mnist.ml;

import fr.mnist.data.PixelVector;
import fr.mnist.exceptions.ModelNotFoundException;

public interface Classifier {
    void train(String arffPath) throws Exception;
    int predict(PixelVector v) throws ModelNotFoundException;
    double getAccuracy();
    String getModelName();
}
