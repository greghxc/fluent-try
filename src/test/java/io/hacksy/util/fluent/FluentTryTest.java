package io.hacksy.util.fluent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FluentTryTest {

  @Nested
  @DisplayName("#get")
  class GetTests {
    @Test
    @DisplayName("Returns result on non-null supplied value")
    void getHappy() {
      FluentTry<String> result = FluentTry.of(() -> "myString");
      assertThat(result.get()).isEqualTo("myString");
    }

    @Test
    @DisplayName("Throws on supplier that throws unchecked exception")
    void getThrownExceptionUnchecked() {
      RuntimeException expectedException = new RuntimeException("BOOM");

      FluentTry<String > result = FluentTry.of(() -> {
        throw expectedException;
      });

      assertThatThrownBy(result::get)
          .isInstanceOf(CaughtException.class)
          .hasRootCause(expectedException);
    }

    @Test
    @DisplayName("Throws on supplier that throws checked exception")
    void getThrownExceptionChecked() {
      Exception expectedException = new ArithmeticException();

      FluentTry<String > result = FluentTry.of(() -> {
        throw expectedException;
      });

      assertThatThrownBy(result::get)
          .isInstanceOf(CaughtException.class)
          .hasRootCause(expectedException);
    }

    @Test
    @DisplayName("Returns empty if supplier returns null")
    void getReturnsNullWhenSupplierReturnsNull() {
      FluentTry<String > result = FluentTry.of(() -> null);
      assertThat(result.get()).isNull();
    }
  }

  @Nested
  @DisplayName("#toOptional")
  class ToOptionalTests {
    @Test
    @DisplayName("Returns non-empty optional when supplier returns value")
    void toOptionalHappyPath() {
      Optional<String> result = FluentTry.of(() -> "myString").toOptional();
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(result).isNotEmpty();
        softly.assertThat(result).contains("myString");
      });
    }

    @Test
    @DisplayName("Returns empty optional when supplier throws")
    void toOptionalOnThrow() {
      FluentTry<String> result = FluentTry.of(() -> {
        throw new RuntimeException("BOOM");
      });
      assertThat(result.toOptional()).isEmpty();
    }

    @Test
    @DisplayName("Returns empty optional when supplier returns null")
    void toOptionalOnNull() {
      FluentTry<String> result = FluentTry.of(() -> null);
      assertThat(result.toOptional()).isEmpty();
    }
  }

  @Nested
  @DisplayName("#toOptionalError")
  class ToOptionalErrorTests {
    @Test
    @DisplayName("Returns empty optional when supplier returns value")
    void toOptionalHappyPath() {
      FluentTry<String> result = FluentTry.of(() -> "myString");
      assertThat(result.toOptionalError()).isEmpty();
    }

    @Test
    @DisplayName("Returns non-empty optional when supplier throws")
    void toOptionalOnThrow() {
      RuntimeException expectedException = new RuntimeException("BOOM");
      FluentTry<String> result = FluentTry.of(() -> {
        throw expectedException;
      });
      assertThat(result.toOptionalError()).isNotEmpty().contains(expectedException);
    }

    @Test
    @DisplayName("Returns empty optional when supplier returns null")
    void toOptionalOnNull() {
      FluentTry<String> result = FluentTry.of(() -> null);
      assertThat(result.toOptionalError()).isEmpty();
    }
  }

  @Nested
  @DisplayName("#onError")
  class OnErrorTests {
    @Test
    @DisplayName("Does not call consumer when supplier returns value")
    void onErrorHappyPath() {
      CheckableErrorConsumer consumer = new CheckableErrorConsumer();
      FluentTry<String> result = FluentTry.of(() -> "myString");
      FluentTry<String> resultAfterError = result.onError(consumer);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(consumer.getCalledWith()).isEmpty();
        softly.assertThat(resultAfterError).isEqualTo(result);
      });

    }

    @Test
    @DisplayName("Calls consumer when supplier throws")
    void onErrorOnThrow() {
      CheckableErrorConsumer consumer = new CheckableErrorConsumer();
      RuntimeException expectedException = new RuntimeException("BOOM");
      FluentTry<String> result = FluentTry.of(() -> {
        throw expectedException;
      });
      FluentTry<String> resultAfterError = result.onError(consumer);
      SoftAssertions.assertSoftly(softly -> {
        softly.assertThat(consumer.getCalledWith()).containsExactly(expectedException);
        softly.assertThat(resultAfterError).isEqualTo(result);
      });
    }
  }

  @Nested
  @DisplayName("#mapError")
  class MapErrorTests {
    @Test
    @DisplayName("Does nothing when supplier didn't throw")
    void mapErrorHappyPath() {
      FluentTry<String> result = FluentTry.of(() -> "myString");
      FluentTry<String> resultAfterError = result
          .mapError(err -> new RuntimeException("Some Message", err));
      assertThat(resultAfterError).isEqualTo(result);
    }

    @Test
    @DisplayName("Calls consumer when supplier throws")
    void mapErrorOnThrow() {
      RuntimeException expectedException = new RuntimeException("BOOM");
      FluentTry<String> result = FluentTry.of(() -> {
        throw expectedException;
      });
      FluentTry<String> resultAfterError = result
          .mapError(err -> new RuntimeException("Some Message", err));
      assertThat(resultAfterError.toOptionalError().get())
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Some Message")
          .hasRootCause(result.toOptionalError().get());
    }
  }

  @Nested
  @DisplayName("#mapErrorToResult")
  class MapErrorToResultTests {
    @Test
    @DisplayName("Does nothing when supplier didn't throw")
    void mapErrorToResultHappyPath() {
      FluentTry<String> result = FluentTry.of(() -> "myString")
          .mapErrorToResult(Throwable::getMessage);
      assertThat(result.get()).isEqualTo("myString");
    }

    @Test
    @DisplayName("Maps to alternate result when supplier throws")
    void mapErrorToResultOnThrow() {
      RuntimeException expectedException = new RuntimeException("BOOM");
      FluentTry<String> result = FluentTry.of(() -> {
        throw expectedException;
      });
      result = result
          .mapErrorToResult(Throwable::getMessage);
      assertThat(result.get()).isEqualTo("BOOM");
    }
  }

  private static class CheckableErrorConsumer implements Consumer<Throwable> {
    private final List<Throwable> calledWith;

    private CheckableErrorConsumer() {
      calledWith = new ArrayList<>();
    }

    @Override
    public void accept(Throwable throwable) {
      calledWith.add(throwable);
    }

    public List<Throwable> getCalledWith() {
      return calledWith;
    }
  }
}