package com.example.hello;

import org.jspecify.annotations.Nullable;

/** Builds greetings. */
public final class Greeter {
  private Greeter() {}

  /**
   * Returns a greeting.
   *
   * @param name who to greet; {@code null} or blank means "world"
   * @return greeting text
   */
  public static String greet(@Nullable String name) {
    String who = name == null || name.isBlank() ? "world" : name.strip();
    return "Hello, " + who + "!";
  }

  /**
   * Entry point.
   *
   * @param args optional name
   */
  public static void main(String[] args) {
    System.out.println(greet(args.length > 0 ? args[0] : null));
  }
}
