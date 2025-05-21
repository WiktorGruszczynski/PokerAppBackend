package org.example.pokerbakend.services.exceptions;

public class IllegalMoveException extends RuntimeException {
  public IllegalMoveException(String message) {
    super(message);
  }
}
