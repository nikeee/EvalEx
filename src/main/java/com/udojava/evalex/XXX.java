package com.udojava.evalex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

public class XXX {
  private final ExpressionLanguage language;
  private final String originalExpression;
  private final ExpressionSettings expressionSettings;
  private final List<ExpressionLanguage.Token> parsedExpression;

  XXX(ExpressionLanguage language, String originalExpression, ExpressionSettings expressionSettings, List<ExpressionLanguage.Token> parsedExpression) {
    this.language = language;
    this.originalExpression = originalExpression;
    this.expressionSettings = expressionSettings;
    this.parsedExpression = parsedExpression;
  }

  @Override
  public String toString() {
    return originalExpression;
  }


  /**
   * Evaluates the expression.
   *
   * @return The result of the expression.
   */
  public BigDecimal eval(final EvaluationSettings settings) {
    return eval(settings, new HashMap<String, Number>());
  }

  public BigDecimal eval(final EvaluationSettings settings, final Map<String, Number> variableScope) {

    Deque<ExpressionLanguage.LazyNumber> stack = new ArrayDeque<ExpressionLanguage.LazyNumber>();

    for (final ExpressionLanguage.Token token : parsedExpression) {
      switch (token.type) {
        case UNARY_OPERATOR: {
          final ExpressionLanguage.LazyNumber value = stack.pop();
          ExpressionLanguage.LazyNumber result = new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              return language.operators.get(token.surface).eval(value, null, settings).eval();
            }

            @Override
            public String getString() {
              return String.valueOf(language.operators.get(token.surface).eval(value, null, settings).eval());
            }
          };
          stack.push(result);
          break;
        }
        case OPERATOR:
          final ExpressionLanguage.LazyNumber v1 = stack.pop();
          final ExpressionLanguage.LazyNumber v2 = stack.pop();
          ExpressionLanguage.LazyNumber result = new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              return language.operators.get(token.surface).eval(v2, v1, settings).eval();
            }

            public String getString() {
              return String.valueOf(language.operators.get(token.surface).eval(v2, v1, settings).eval());
            }
          };
          stack.push(result);
          break;
        case VARIABLE:
          if (!variableScope.containsKey(token.surface)) {
            throw new ExpressionException("Unknown operator or function: " + token);
          }

          stack.push(new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              Number variable = variableScope.get(token.surface);
              if (variable == null) {
                return null;
              }
              if (variable instanceof BigDecimal) {
                return (BigDecimal) variable;
              }

              return new BigDecimal(variable.toString(), settings.getMathContext());
            }

            public String getString() {
              return token.surface;
            }
          });
          break;
        case FUNCTION:
          com.udojava.evalex.LazyFunction f = language.functions.get(token.surface.toUpperCase(Locale.ROOT));
          ArrayList<ExpressionLanguage.LazyNumber> p = new ArrayList<ExpressionLanguage.LazyNumber>(
              !f.numParamsVaries() ? f.getNumParams() : 0);
          // pop parameters off the stack until we hit the start of
          // this function's parameter list
          while (!stack.isEmpty() && stack.peek() != ExpressionLanguage.PARAMS_START) {
            p.add(0, stack.pop());
          }

          if (stack.peek() == ExpressionLanguage.PARAMS_START) {
            stack.pop();
          }

          ExpressionLanguage.LazyNumber fResult = f.lazyEval(p, settings);
          stack.push(fResult);
          break;
        case OPEN_PAREN:
          stack.push(ExpressionLanguage.PARAMS_START);
          break;
        case LITERAL:
          stack.push(new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              if (token.surface.equalsIgnoreCase("NULL")) {
                return null;
              }

              return new BigDecimal(token.surface, settings.getMathContext());
            }

            public String getString() {
              return String.valueOf(new BigDecimal(token.surface, settings.getMathContext()));
            }
          });
          break;
        case STRINGPARAM:
          stack.push(new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              return null;
            }

            public String getString() {
              return token.surface;
            }
          });
          break;
        case HEX_LITERAL:
          stack.push(new ExpressionLanguage.LazyNumber() {
            public BigDecimal eval() {
              return new BigDecimal(new BigInteger(token.surface.substring(2), 16), settings.getMathContext());
            }

            public String getString() {
              return new BigInteger(token.surface.substring(2), 16).toString();
            }
          });
          break;
        default:
          throw new ExpressionException("Unexpected token " + token.surface, token.pos);
      }
    }

    BigDecimal result = stack.pop().eval();
    if (result == null) {
      return null;
    }

    return settings.getStripTrailingZeros()
        ? result.stripTrailingZeros()
        : result;
  }


  /**
   * Checks whether the expression is a boolean expression. An expression is considered a boolean
   * expression, if the last operator or function is boolean. The IF function is handled special. If
   * the third parameter is boolean, then the IF is also considered boolean, else non-boolean.
   *
   * @return <code>true</code> if the last operator/function was a boolean.
   */
  public boolean isBoolean() {
    if (!parsedExpression.isEmpty()) {
      for (int i = parsedExpression.size() - 1; i >= 0; i--) {
        ExpressionLanguage.Token t = parsedExpression.get(i);
        /*
         * The IF function is handled special. If the third parameter is
         * boolean, then the IF is also considered a boolean. Just skip
         * the IF function to check the second parameter.
         */
        if (t.surface.equals("IF")) {
          continue;
        }
        if (t.type == ExpressionLanguage.TokenType.FUNCTION) {
          return language.functions.get(t.surface).isBooleanFunction();
        } else if (t.type == ExpressionLanguage.TokenType.OPERATOR) {
          return language.operators.get(t.surface).isBooleanOperator();
        }
      }
    }
    return false;
  }

}

class EvaluationSettings {
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