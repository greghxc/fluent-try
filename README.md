# fluent-try

A simple library to address the inconvenience of using Java try/catch logic in fluent and/or
functional APIs without including Vavr, Reactor or other very capable libraries.

This utility has no additional dependencies and supports Java 8+.

## Usage Examples

### General Usage

***Before:***
```java
import io.hacksy.util.fluent.FluentTry;

public class Main {
    public static void main(String[]args){
        String myValue;
        
        try {
          myValue = volatileFunction();
        } catch (JsonProcessingException jpe) {
          log.error(jpe.getMessage());
          throw new RuntimeException(jpe);
        }

        String finalValue = myValue.toUpperCase();
    }
}
```

***After:***
```java
import io.hacksy.util.fluent.FluentTry;

public class Main {
    public static void main(String[]args) {
        String finalValue = FluentTry.of(() -> volatileFunction())
            .onError(err -> log.error(err.getMessage()))
            .toOptional()
            .map(String::toUpperCase)
            .get();
    }
}
```