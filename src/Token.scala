enum TokenType(val kind: String):
  case VAL extends TokenType("VAL")
  case VAR extends TokenType("VAR")
  case RETURN extends TokenType("RETURN")
  case IF extends TokenType("IF")
  case ELSE extends TokenType("ELSE")
  case WHILE extends TokenType("WHILE")
  case BREAK extends TokenType("BREAK")
  case CONTINUE extends TokenType("CONTINUE")
  case TRUE extends TokenType("TRUE")
  case FALSE extends TokenType("FALSE")

  case IDENT extends TokenType("IDENT")
  case INT_LITERAL extends TokenType("INT")

  case PLUS extends TokenType("PLUS")
  case MINUS extends TokenType("MINUS")
  case STAR extends TokenType("MULT")
  case SLASH extends TokenType("DIV")

  case EQ extends TokenType("EQ")
  case NE extends TokenType("NE")
  case LT extends TokenType("LT")
  case GT extends TokenType("GT")
  case LE extends TokenType("LE")
  case GE extends TokenType("GE")

  case AND extends TokenType("AND")
  case OR extends TokenType("OR")
  case NOT extends TokenType("NOT")

  case ASSIGN extends TokenType("ASSIGN")
  case LPAREN extends TokenType("LPAREN")
  case RPAREN extends TokenType("RPAREN")
  case LBRACE extends TokenType("LBRACE")
  case RBRACE extends TokenType("RBRACE")
  case SEMICOLON extends TokenType("SEMI")

  case EOF extends TokenType("EOF")
  case ERROR extends TokenType("ERROR")

case class Token(
  tokenType: TokenType,
  lexeme: String,
  line: Int,
  column: Int,
  value: Option[Long] = None,
  errorMessage: Option[String] = None
):
  def kind: String = tokenType.kind