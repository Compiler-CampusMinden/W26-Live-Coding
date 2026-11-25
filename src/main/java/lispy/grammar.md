
# Lispy


## Programme in Lispy L-Int

Nur Integer, Addition und Subtraktion:

```clojure
42
( + 32 42 23)
(- 20 5 3)   ;; (- (- 20 5) 3)
(+ 2 3) 444  ;; nicht erlaubt!
```

### Konkrete Syntax

Wie sehen die Eingabe-Schnipsel aus?

```ebnf
eform ::= INT | '(' '+' eform eform+ ')' | '(' '-' eform eform+ ')'
INT   ::= [0-9]+

lint  ::= eform EOF
```

(Leerzeichen und Kommentare lassen wir in dieser Form der konkreten Grammatik erstmal weg erstmal - das greifen wir beim Lexer/Parser wieder auf.)

### Abstrakte Syntax

```ebnf
Expr = AddExpr(Expr, Expr) | SubExpr(Expr, Expr) | IntLiteral(Int)
Lint = Expr
```


## Programme in Lispy L-Expr

Zusätzlich **Multiplikation** und **Subtraktion** sowie **unäres Minus**:

```clojure
( * 32 42 23)
(/ 20 5 3)   ;; (/ (/ 20 5) 3)
(* 2 (+ 3 4))
(- 2)
```

### Konkrete Syntax

```ebnf
eform ::= INT | '(' '+' eform eform+ ')' | '(' '-' eform eform+ ')'
INT   ::= [0-9]+
---

eform ::= '(' '*' eform eform+ ')'
        | '(' '/' eform eform+ ')'
        | '(' '-' eform ')'

lexpr ::= eform EOF
```

### Abstrakte Syntax

``` ebnf
Expr  = IntLiteral(Int) | AddExpr(Expr, Expr) | SubExpr(Expr, Expr)
---

Expr  = MulExpr(Expr, Expr) | DivExpr(Expr, Expr)
Lexpr = Expr
```

### Vorrang und Assoziativität: Gestufte Grammatik-Regeln

``` ebnf
expr  ::= expr '*' expr | expr '+' expr | '-' expr | INT
```

``` ebnf
expr  ::= term ('+' term)*
term  ::= atom ('*' atom)*
atom  ::= '-' atom
        | INT
```

### ANTLR Visitor

``` java
public class LispyAstBuilderLexpr extends LispyBaseVisitor<Object> {

    @Override
    public Expr visitLexpr(LexprContext ctx) { return asExpr(visit(ctx.eform())); }

    @Override
    public Expr visitIntLit(IntLitContext ctx) {
        int value = Integer.parseInt(ctx.INT().getText());
        return new IntLiteral(value);
    }

    @Override
    public Expr visitAddExpr(AddExprContext ctx) {
        List<Expr> ops = ctx.eform().stream()
                            .map(this::visit)
                            .map(this::asExpr).toList();
        return ops.stream().reduce(AddExpr::new).orElseThrow();
    }
}
```

### ANTLR Pipeline

``` java
var source = "(+ 1 2)";

var input = CharStreams.fromString(source);
var lexer = new LispyLexer(input);
var tokens = new CommonTokenStream(lexer);

var parser = new LispyParser(tokens);
var tree = parser.lexpr();

var builder = new LispyAstBuilderLexpr();
var ast = builder.visitLexpr(tree);
```
