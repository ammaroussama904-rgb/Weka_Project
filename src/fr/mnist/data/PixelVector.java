package fr.mnist.data;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class PixelVector {
    private int[] raw;

    public PixelVector(int[] raw) {
        this.raw = raw;
    }

    public double[] normalize() {
        double[] normalized = new double[raw.length];
        for (int i = 0; i < raw.length; i++) {
            // Convertir de [0-255] vers [0-1]
            normalized[i] = raw[i] / 255.0;
        }
        return normalized;
    }

    public Instance toWekaInstance(Instances dataset) {
        Instance inst = new DenseInstance(1.0, normalize());
        inst.setDataset(dataset);
        return inst;
    }
}