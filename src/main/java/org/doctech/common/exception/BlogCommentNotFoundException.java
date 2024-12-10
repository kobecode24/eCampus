package org.doctech.common.exception;

public class BlogCommentNotFoundException extends RuntimeException {
    public BlogCommentNotFoundException(String message) {
        super(message);
    }
}
