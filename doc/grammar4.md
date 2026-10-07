# SysProLang

Version: 4

## Grammar

```bnf
program ::= { topDeclaration } EOF

topDeclaration ::= externDeclaration
                 | funcDeclaration

externDeclaration ::=
    "extern" "def" IDENT "(" [ typedParamList ] ")" ":" returnType ";"

funcDeclaration ::=
    "def" IDENT "(" [ typedParamList ] ")" ":" returnType block

typedParamList ::= IDENT ":" type { "," IDENT ":" type }


; Type definitions

type ::= "Int8" | "Int16" | "Int32" | "Int64"
       | "Bool"
       | "String"


; returnType is the return type of functions and extern declarations. Void is
; allowed only here: it is not part of `type`, so variable declarations,
; parameter types, and any other type position reject it syntactically.
returnType ::= "Void"
           | type


statement ::=
    returnStatement
  | declarationStatement
  | assignmentStatement
  | expressionStatement
  | ifStatement
  | whileStatement
  | breakStatement
  | continueStatement
  | block

block ::= "{" { statement } "}"

returnStatement ::= "return" [ expression ] ";"

declarationStatement ::=
    "val" IDENT ":" type "=" expression ";"
  | "var" IDENT ":" type "=" expression ";"

assignmentStatement ::= IDENT "=" expression ";"

expressionStatement ::= expression ";"

ifStatement ::=
    "if" "(" expression ")" statement
    [ "else" statement ]

whileStatement ::= "while" "(" expression ")" statement

breakStatement ::= "break" ";"

continueStatement ::= "continue" ";"

; Expression definitions go from lowest operator precedence
; to the highest, allowing for straightforward expression parsing.
; Comparison operators produce Bool values.
; Logical operators && and || are short-circuit.
; Function call has the highest precedence (tight binding).
; Cast expression binds like a primary expression.
expression ::= logicalOrExpression

logicalOrExpression ::=
    logicalAndExpression { "||" logicalAndExpression }

logicalAndExpression ::=
    equalityExpression { "&&" equalityExpression }

equalityExpression ::=
    relationalExpression { ("==" | "!=") relationalExpression }

relationalExpression ::=
    additiveExpression { ("<" | ">" | "<=" | ">=") additiveExpression }

additiveExpression ::=
    multiplicativeExpression { ("+" | "-") multiplicativeExpression }

multiplicativeExpression ::=
    unaryExpression { ("*" | "/") unaryExpression }

unaryExpression ::=
    "!" unaryExpression
  | "-" unaryExpression
  | callExpression

callExpression ::=
    primaryExpression { "(" [ argumentList ] ")" }

argumentList ::= expression { "," expression }

primaryExpression ::=
    INTEGER_LITERAL
  | STRING_LITERAL
  | IDENT
  | "true"
  | "false"
  | "(" expression ")"
  | "cast" "<" type ">" "(" expression ")"


; Lexical tokens

INTEGER_LITERAL ::=
    "0"
  | NON_ZERO_DIGIT { DIGIT }

STRING_LITERAL ::=
    '"' { CHARACTER | ESCAPE_SEQUENCE } '"'

ESCAPE_SEQUENCE ::=
    "\\" ( 'n' | 't' | '\\' | '"' )

CHARACTER ::= (any character except newline, backslash, or double quote)

IDENT ::= NON_DIGIT { (NON_DIGIT | DIGIT) }

NON_DIGIT ::=
    "a" | "b" | ... | "z"
  | "A" | "B" | ... | "Z"
  | "_"

DIGIT ::= "0" | NON_ZERO_DIGIT

NON_ZERO_DIGIT ::=
    "1" | "2" | ... | "9"