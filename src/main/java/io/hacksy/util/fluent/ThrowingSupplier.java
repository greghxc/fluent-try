package io.hacksy.util.fluent;

/**
 * An interface that allows a {@link FluentTry} to accept an anonymous function that throws a
 * checked exception without explicitly handling it.
 *
 * @param <T> the type parameter
 */
public interface ThrowingSupplier<T> {

  /**
   * Execute the supplier. The supplier can throw a checked exception without explicit handing.
   *
   * @return the returned value
   * @throws Throwable the throwable, if thrown
   */
  T get() throws Throwable;
}
