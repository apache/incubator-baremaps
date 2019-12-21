package io.gazetteer.osm.stream;

import static org.junit.jupiter.api.Assertions.*;

import io.gazetteer.osm.stream.Try.Success;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;

class TryTest {

  private final Object object = new Object();

  private final Exception exception = new Exception();

  private final Callable<Object> success = () -> object;

  private final Callable<Object> failure = () -> {
    throw exception;
  };

  @Test
  void of() {
    assertTrue(Try.of(success) instanceof Try.Success);
    assertTrue(Try.of(failure) instanceof Try.Failure);
  }

  @Test
  void isSuccess() {
    assertTrue(Try.of(success).isSuccess());
    assertFalse(Try.of(failure).isSuccess());
  }

  @Test
  void value() {
    assertEquals(Try.of(success).value(), object);
    assertEquals(Try.of(failure).value(), null);
  }

  @Test
  void exception() {
    assertEquals(Try.of(success).exception(), null);
    assertEquals(Try.of(failure).exception(), exception);
  }
}