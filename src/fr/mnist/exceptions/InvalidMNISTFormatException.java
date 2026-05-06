package fr.mnist.exceptions;

public class InvalidMNISTFormatException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidMNISTFormatException() { super(); }
    public InvalidMNISTFormatException(String message) { super(message); }
}