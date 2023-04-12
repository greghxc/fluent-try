# fluent-try

A simple library to address the inconvenience of using Java try/catch logic in fluent and/or
functional APIs without including Vavr, Reactor or other very capable libraries.

This utility has no additional dependencies and supports Java 8+.

## Usage Examples

### As "Sneaky Throws"
***Before:***
```java
List<MyPojo> myStuff = jsonStrings.stream()
    .map(json -> {
        try {
            return mapper.readValue(json, MyPojo.class);
        } catch (JsonProcessingException jpe) {
            throw new RunTimeException(jpe);
        })
    .toList();
```

***After:***
```java
List<MyPojo> myStuff = jsonStrings.stream()
    .map(json -> FluentTry.of(() -> mapper.readValue(json, MyPojo.class)).get())
    .toList();
```

### To Optional with Logging
***Before:***
```java
Optional<MyPojo> maybePojo;

try {
    maybePojo = Optional.of(mapper.readValue(json, MyPojo.class));
} catch (JsonProcessingException jpe) {
    log.error(jpe.getMessage());
    maybePojo = Optional.empty();    
```

***After:***
```java
Optional<MyPojo> maybePojo = FluentTry.of(mapper.readValue(json, MyPojo.class))
    .onError(err -> log.error(err.getMessage())
    .toOptional();
```
