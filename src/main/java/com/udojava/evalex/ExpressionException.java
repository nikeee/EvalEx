package com.udojava.evalex;

/**
 * The expression evaluators exception class.
 */
public class ExpressionException extends RuntimeException {

  private static final long serialVersionUID = 1118142866870779047L;

  public ExpressionException(String message) {
    super(message);
  }

  public ExpressionException(String message, int characterPosition) {
    super(message + " at character position " + characterPosition);
  }
}
