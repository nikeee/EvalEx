package com.udojava.evalex;

import java.math.MathContext;

public class EvaluationSettings {
  private final MathContext mathContext;
  private final boolean stripTrailingZeros;


  /**
   * @param mathContext
   * @param stripTrailingZeros If set to <code>true</code> trailing zeros in the result are stripped.
   */
  public EvaluationSettings(MathContext mathContext, boolean stripTrailingZeros) {
    this.mathContext = mathContext;
    this.stripTrailingZeros = stripTrailingZeros;
  }


  public MathContext getMathContext() {
    return mathContext;
  }


  public boolean getStripTrailingZeros() {
    return stripTrailingZeros;
  }
}