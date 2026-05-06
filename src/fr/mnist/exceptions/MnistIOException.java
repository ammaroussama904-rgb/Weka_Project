package fr.mnist.exceptions;

import java.io.IOException;

public class MnistIOException extends IOException {
    private static final long serialVersionUID = 1L;
    public MnistIOException() { super(); }
    public MnistIOException(String message) { super(message); }
    public MnistIOException(String message, Throwable cause) { super(message, cause); }
}