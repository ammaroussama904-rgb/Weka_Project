package fr.mnist.exceptions;

public class ModelNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    public ModelNotFoundException() { super(); }
    public ModelNotFoundException(String message) { super(message); }
}