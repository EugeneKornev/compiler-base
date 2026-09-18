enum TokenType(val kind: String):
  case VAL extends TokenType("VAL")
  case VAR extends TokenType("VAR")
  case RETURN extends TokenType("RETURN")

  case IDENT extends TokenType("IDENT")
  case INT_LITERAL extends TokenType("INT")

  case PLUS extends TokenType("PLUS")
  case MINUS extends TokenType("MINUS")
  case STAR extends TokenType("MULT")   
  case SLASH extends TokenType("DIV")   
  case ASSIGN extends TokenType("EQ")
  case LPAREN extends TokenType("LPAREN")
  case RPAREN extends TokenType("RPAREN")
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