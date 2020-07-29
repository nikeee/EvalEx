package com.udojava.evalex;

import java.math.MathContext;

public class Main {
  public static void main(String[] args) {

    ExpressionLanguage l = new ExpressionLanguage();

    ExpressionSettings s = new ExpressionSettings(1);
    EvaluationSettings evalSettings = new EvaluationSettings(MathContext.DECIMAL32, true);

    XXX expr = l.parseExpression("1+1", s);

    System.out.println(expr.eval(evalSettings));
  }
}
