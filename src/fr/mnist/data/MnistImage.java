package fr.mnist.data;

public class MnistImage {
    private int[] pixels;
    private int label;

    public MnistImage(int[] pixels, int label) {
        this.pixels = pixels.clone();
        this.label = label;
    }

    public int[] getPixels() { return pixels.clone(); }
    public int getLabel() { return label; }
}