package com.example.hello;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreeterTest {
  @Test
  void greetsByName() {
    assertEquals("Hello, Ann!", Greeter.greet(" Ann "));
  }

  @Test
  void defaultsToWorld() {
    assertEquals("Hello, world!", Greeter.greet(null));
  }

  @Test
  void mainRuns() {
    assertDoesNotThrow(() -> Greeter.main(new String[] {"Bob"}));
    assertDoesNotThrow(() -> Greeter.main(new String[0]));
  }
}
