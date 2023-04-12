package io.hacksy.util.fluent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class FluentTry<T> {
  private final Throwable throwable;
  private final T result;

  private FluentTry(T result, Throwable throwable) {
    this.result = result;
    this.throwable = throwable;
  }

  static <T> FluentTry<T> of(ThrowingSupplier<T> throwingSupplier) {
    try {
      return new FluentTry<>(throwingSupplier.get(), null);
    } catch (Throwable t) {
      return new FluentTry<>(null, t);
    }
  }

  public T get() throws CaughtException {
    if (Objects.nonNull(throwable)) {
      throw new CaughtException(throwable);
    }  else {
      return result;
    }
  }

  public FluentTry<T> onError(Consumer<Throwable> throwableConsumer) {
    if (Objects.nonNull(throwable)) {
      throwableConsumer.accept(throwable);
    }
    return this;
  }

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

  public Optional<T> toOptional() {
    if (Objects.nonNull(throwable)) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(result);
    }
  }

  public Optional<Throwable> toOptionalError() {
    return Optional.ofNullable(throwable);
  }
}
