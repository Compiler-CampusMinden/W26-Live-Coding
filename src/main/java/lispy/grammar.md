
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

