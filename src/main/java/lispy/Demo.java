package lispy;

// ---------------------------
// Common
// ---------------------------

// ---------------------------
// L-Int
// ---------------------------

// === AST ===

interface Expr {}

record IntLiteral(int val) implements Expr {}
record AddExpr(Expr lhs, Expr rhs) implements Expr {}
record SubExpr(Expr lhs, Expr rhs) implements Expr {}

// === Runtime Values ===

interface Value {}
record IntVal(int v) implements Value {}

// === Interpreter ===

class InterpreterLint {
  public Value eval(Expr expr) {
    return switch (expr) {
      case IntLiteral(var val) -> new IntVal(val);

      case AddExpr(var lhs, var rhs) -> {
        int a = asInt(eval(lhs));
        int b = asInt(eval(rhs));
        yield new IntVal(a + b);
      }

      // TODO: SubExpr

      default -> throw new RuntimeException("unexpected value: " + expr);
    };
  }

  protected int asInt(Value v) {
    if (v instanceof IntVal(var val)) return val;
    throw new RuntimeException("number expected");
  }
}

// ---------------------------
// Starter
// ---------------------------

public class Demo {
  static void main() {
    String source = "( + (+ 4 2) (+ 1 3))";

    Expr ast =
        new AddExpr(
            new AddExpr(new IntLiteral(4), new IntLiteral(2)),
            new AddExpr(new IntLiteral(1), new IntLiteral(3)));

    var interpreter = new InterpreterLint();
    Value val = interpreter.eval(ast);

    IO.println(val);
  }
}
