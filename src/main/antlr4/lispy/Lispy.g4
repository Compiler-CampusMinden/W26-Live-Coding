grammar Lispy;

lexpr : eform EOF ;

eform : '(' '*' eform eform+ ')'   #mulExpr
      | '(' '/' eform eform+ ')'   #divExpr
      | '(' '+' eform eform+ ')'   #addExpr
      | '(' '-' eform eform+ ')'   #subExpr
      | '(' '-' eform ')'          #unMinusExpr
      | INT                        #numberExpr
      ;


INT   : [0-9]+ ;

WS    : [ \t\n\r] -> skip ;
