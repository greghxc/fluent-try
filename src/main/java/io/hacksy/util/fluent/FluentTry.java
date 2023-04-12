package io.hacksy.util.fluent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * A utility class for representing try/catch logic to handle both checked and unchecked
 * exceptions in fluent or functional workflows or anywhere a try/catch is inconvenient.
 *
 * @param <T> the type parameter
 */
public class FluentTry<T> {
  private final Throwable throwable;
  private final T result;

  private FluentTry(T result, Throwable throwable) {
    this.result = result;
    this.throwable = throwable;
  }

  /**
   * The primary way to create a FluentTry of Type by passing in an anonymous function that returns
   * Type. The anonymous function can also through checker or unchecked exceptions. The anonymous
   * function is executed immediately; that is the function is executed even if the FluentTry
   * is never evaluated with {@link #get() Get} or {@link #toOptional ToOptional} or other
   * methods.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> "myString");
   * try.get(); // "myString"
   * }</pre>
   *
   * @param <T>              the type parameter
   * @param throwingSupplier the throwing supplier
   * @return the new {@link FluentTry}
   */
  static <T> FluentTry<T> of(ThrowingSupplier<T> throwingSupplier) {
    try {
      return new FluentTry<>(throwingSupplier.get(), null);
    } catch (Throwable t) {
      return new FluentTry<>(null, t);
    }
  }

  /**
   * Return the result of the supplier provided on construction, <i>including null</i>. If the
   * supplier threw an exception on execution, it will be wrapped in CaughtException and thrown.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> "myString");
   * try.get(); // "myString"
   * }</pre>
   *
   * @return the result of the supplier, including null
   * @throws CaughtException the wrapped exception if thrown by the supplier
   */
  public T get() throws CaughtException {
    if (Objects.nonNull(throwable)) {
      throw new CaughtException(throwable);
    }  else {
      return result;
    }
  }

  /**
   * An entry point to perform a side effect on the caught exception. The consumer is only executed
   * if the supplier threw on execution.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> volatileFunction());
   * try.onError(err -> log.error(err.getMessage());
   * }</pre>
   *
   * @param throwableConsumer consumer to perform a side effect on a caught error
   * @return the unchanged {@link FluentTry}
   */
  public FluentTry<T> onError(Consumer<Throwable> throwableConsumer) {
    if (Objects.nonNull(throwable)) {
      throwableConsumer.accept(throwable);
    }
    return this;
  }

  /**
   * An entry point to map from one error to another. Only executed if the supplier threw on
   * exception.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> volatileFunction());
   * try.mapError(err -> new RuntimeException(err));
   * }</pre>
   *
   * @param throwableMapper the error mapping function
   * @return the {@link FluentTry} with the mapped error if error was present
   */
  public FluentTry<T> mapError(UnaryOperator<Throwable> throwableMapper) {
    if (Objects.nonNull(throwable)) {
      return new FluentTry<>(
          result,
          throwableMapper.apply(throwable)
      );
    } else {
      return this;
    }
  }

  /**
   * An entry point to provide a function to map a thrown error to a fallback value of Type. Only
   * use this method if the fallback value makes uses of the thrown exception. Otherwise, use
   * {@link #toOptional() ToOptional} with {@link Optional#orElse(Object) OrElse}.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> volatileFunction());
   * try.mapErrorToResult(err -> "fallback"));
   * }</pre>
   *
   * @param throwableMapper the error mapping function
   * @return the {@link FluentTry} with the fallback value if error was present
   */
  public FluentTry<T> mapErrorToResult(Function<Throwable, T> throwableMapper) {
    if (Objects.nonNull(throwable)) {
      return new FluentTry<>(
          throwableMapper.apply(throwable),
          null
      );
    } else {
      return this;
    }
  }

  /**
   * Return an optional based on the outcome of the supplied provided on construction. Null values
   * or thrown exceptions return an empty {@link Optional}. Otherwise and {@link Optional} of the
   * supplied value is returned.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> "myString");
   * try.toOptional(); // Optional.of("myString")
   * }</pre>
   *
   * @return {@link Optional} of empty or the supplied value
   */
  public Optional<T> toOptional() {
    if (Objects.nonNull(throwable)) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(result);
    }
  }

  /**
   * Return an optional based on the outcome of the supplied provided on construction. Anything
   * other than an exception returns an empty {@link Optional}. Otherwise, an {@link Optional} of
   * the thrown error is returned.
   * <br><br>
   * <b>Example:</b>
   * <pre>{@code
   * FluentTry<String> try = FluentTry.of(() -> volatileFunction());
   * try.toOptionalError(); // Optional.of(SomeException)
   * }</pre>
   *
   * @return an optional of the thrown error
   */
  public Optional<Throwable> toOptionalError() {
    return Optional.ofNullable(throwable);
  }
}
