package lispy;

import static lispy.TokenKind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

// ---------------------------
// Common
// ---------------------------

// === Token ===

enum TokenKind {
  PLUS,
  MINUS,
  LPAREN,
  RPAREN,
  INT,
  EOF
}

record Token(TokenKind kind, String lexeme) {}

// === Lexer ===

class LexerLispy {
  String source;
  int current;

  public List<Token> scan(String input) {
    source = input;
    current = 0;

    List<Token> result = new ArrayList<>();

    do result.add(nextToken());
    while (result.getLast().kind() != EOF);

    return result;
  }

  private Token nextToken() {
    skipIrrelevant();

    if (!isAtEnd()) {
      char c = advance();

      if (Character.isDigit(c)) {
        String lex = c + readWhile(Character::isDigit);
        return new Token(INT, lex);
      }

      return switch (c) {
        case '(' -> new Token(LPAREN, "(");
        case ')' -> new Token(RPAREN, ")");
        case '+' -> new Token(PLUS, "+");
        case '-' -> new Token(MINUS, "-");
        default -> throw new RuntimeException("unexpected char: " + c);
      };
    }

    return new Token(EOF, "<EOF>");
  }

  // internal plumbing
  protected boolean isAtEnd() {
    return current >= source.length();
  }

  protected char peek() {
    return peek(0);
  }

  protected char peek(int la) {
    if (la < 0 || current + la >= source.length()) return '\0';
    return source.charAt(current + la);
  }

  protected char advance() {
    return isAtEnd() ? '\0' : source.charAt(current++);
  }

  protected void skipIrrelevant() {
    while (!isAtEnd()) {
      switch (peek()) {
        case ' ', '\t', '\n', '\r' -> readWhile(Character::isWhitespace);
        case ';' -> {
          if (peek(1) == ';') readWhile(ch -> ch != '\n' && ch != '\r');
          else return;
        }
        default -> {
          return;
        }
      }
    }
  } // Homework

  protected String readWhile(Predicate<Character> pred) {
    StringBuilder sb = new StringBuilder();
    while (!isAtEnd() && pred.test(peek())) sb.append(advance());
    return sb.toString();
  }
}

// === Parser ===

class RdParserLispy {
  List<Token> tokens;
  int current;

  public Expr parseLint(List<Token> input) {
    tokens = input;
    current = 0;

    Expr result = parseEform();
    match(EOF);

    return result;
  }

  private Expr parseEform() {
    if (check(INT)) return parseAtom();

    if (check(LPAREN)) {
      if (peek(1).kind() == PLUS) return parseAdd();
      if (peek(1).kind() == MINUS) {} // TODO
    }

    throw new RuntimeException("unexpected token: " + peek());
  }

  // [0-9]+
  private Expr parseAtom() {
    Token t = match(INT);
    return new IntLiteral(Integer.parseInt(t.lexeme()));
  }

  // '(' '+' expr expr+ ')'
  private Expr parseAdd() {
    advance(); // match(TokenKind.LPAREN);
    advance(); // match(TokenKind.PLUS);

    List<Expr> ops = new ArrayList<>();
    ops.add(parseEform()); // first
    ops.add(parseEform()); // second
    while (!check(RPAREN)) ops.add(parseEform());

    advance(); // match(TokenKind.RPAREN);

    return ops.stream().reduce(AddExpr::new).orElseThrow();
  }

  // internal plumbing
  protected boolean isAtEnd() {
    return peek().kind() == EOF;
  }

  protected Token peek() {
    return peek(0);
  }

  protected Token peek(int la) {
    if (la < 0 || current + la >= tokens.size()) return tokens.getLast(); // EOF
    return tokens.get(current + la);
  }

  protected Token advance() {
    return isAtEnd() ? tokens.getLast() : tokens.get(current++);
  }

  protected boolean check(TokenKind... expected) {
    return Arrays.stream(expected).anyMatch(e -> e == peek().kind());
  }

  protected Token match(TokenKind... expected) {
    if (check(expected)) return advance();
    throw new RuntimeException("unexpected token: " + peek());
  }
}

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

    LexerLispy lexer = new LexerLispy();
    List<Token> tokens = lexer.scan(source);

    RdParserLispy parser = new RdParserLispy();
    Expr ast = parser.parseLint(tokens);

    //     Expr ast =
    //            new AddExpr(
    //                new AddExpr(new IntLiteral(4), new IntLiteral(2)),
    //                new AddExpr(new IntLiteral(1), new IntLiteral(3)));

    var interpreter = new InterpreterLint();
    Value val = interpreter.eval(ast);

    IO.println(val);
  }
}
