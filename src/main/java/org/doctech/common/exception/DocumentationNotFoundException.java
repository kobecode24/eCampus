package org.doctech.common.exception;

public class DocumentationNotFoundException extends RuntimeException {
    public DocumentationNotFoundException(String message) {
        super(message);
    }
}
