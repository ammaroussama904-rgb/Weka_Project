package fr.mnist.exceptions;

public class DimensionMismatchException extends Exception {
    private static final long serialVersionUID = 1L;
    public DimensionMismatchException() { super(); }
    public DimensionMismatchException(String message) { super(message); }
}