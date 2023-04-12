package io.hacksy.util.fluent;

public interface ThrowingSupplier<T> {
  T get() throws Throwable;
}
