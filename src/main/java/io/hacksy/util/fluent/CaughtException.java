package io.hacksy.util.fluent;

/**
 * An exception wrapper for throwables caught in the context of a {@link FluentTry}.
 */
public class CaughtException extends RuntimeException {
  CaughtException(Throwable cause) {
    super(cause);
  }
}
