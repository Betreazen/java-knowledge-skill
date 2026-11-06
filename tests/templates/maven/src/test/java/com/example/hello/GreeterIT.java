package com.example.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GreeterIT {
  @Test
  void blankFallsBack() {
    assertEquals("Hello, world!", Greeter.greet("   "));
  }
}
