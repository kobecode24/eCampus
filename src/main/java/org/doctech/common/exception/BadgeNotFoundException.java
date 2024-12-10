package org.doctech.common.exception;

public class BadgeNotFoundException extends RuntimeException {
  public BadgeNotFoundException(String message) {
    super(message);
  }
}
