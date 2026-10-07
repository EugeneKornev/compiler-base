class Lexer(source: String):
  private var pos = 0
  private var line = 1
  private var col = 1
  var hasErrors: Boolean = false

  private val keywords: Map[String, TokenType] = Map(
    "val" -> TokenType.VAL,
    "var" -> TokenType.VAR,
    "return" -> TokenType.RETURN
  )

  def nextToken(): Token = 
    val commentError = skipWhitespaceAndComments()
    if commentError.isDefined then
      hasErrors = true
      return commentError.get

    if isAtEnd() then
      return Token(TokenType.EOF, "", line, col)

    val startCol = col
    val c = eatToken()

    c match 
      case '+' => makeToken(TokenType.PLUS, "+", startCol)
      case '-' => makeToken(TokenType.MINUS, "-", startCol)
      case '*' => makeToken(TokenType.STAR, "*", startCol)
      case '/' => makeToken(TokenType.SLASH, "/", startCol)
      case '=' => makeToken(TokenType.ASSIGN, "=", startCol)
      case '(' => makeToken(TokenType.LPAREN, "(", startCol)
      case ')' => makeToken(TokenType.RPAREN, ")", startCol)
      case ';' => makeToken(TokenType.SEMICOLON, ";", startCol)

      case '0' =>
        makeToken(TokenType.INT_LITERAL, "0", startCol, Some(0L))

      case d if d >= '1' && d <= '9' =>
        lexNumber(d, startCol)

      case a if isAlpha(a) =>
        lexIdentifier(a, startCol)

      case _ =>
        hasErrors = true
        Token(TokenType.ERROR, c.toString, line, startCol, errorMessage = Some(s"Invalid character: '$c'"))

  private def lexNumber(firstChar: Char, startCol: Int): Token =
    val sb = StringBuilder(firstChar.toString)
    while !isAtEnd() && peek().isDigit do
      sb.append(eatToken())

    val text = sb.toString
    val parsedValue = try
      Some(text.toLong)
    catch
      case _: NumberFormatException =>
        hasErrors = true
        None

    makeToken(TokenType.INT_LITERAL, text, startCol, parsedValue)

  private def lexIdentifier(firstChar: Char, startCol: Int): Token =
    val sb = StringBuilder(firstChar.toString)
    while !isAtEnd() && isAlphaNumeric(peek()) do
      sb.append(eatToken())

    val text = sb.toString
    val tokenType = keywords.getOrElse(text, TokenType.IDENT)
    makeToken(tokenType, text, startCol)

  @scala.annotation.tailrec
  private def skipWhitespaceAndComments(): Option[Token] =
    if isAtEnd() then return None

    peek() match
      case ' ' | '\t' | '\r' =>
        eatToken()
        skipWhitespaceAndComments()

      case '\n' =>
        eatToken()
        line += 1
        col = 1
        skipWhitespaceAndComments()

      case '/' if peekNext() == '/' =>
        while !isAtEnd() && peek() != '\n' do
          eatToken()
        skipWhitespaceAndComments()

      case '/' if peekNext() == '*' =>
        val startLine = line
        val startCol = col
        eatToken()
        eatToken()
        var closed = false
        while !isAtEnd() && !closed do
          if peek() == '\n' then
            eatToken()
            line += 1
            col = 1
          else if peek() == '*' && peekNext() == '/' then
            eatToken()
            eatToken()
            closed = true
          else
            eatToken()

        if !closed then
          Some(Token(TokenType.ERROR, "/*", startLine, startCol, errorMessage = Some("Unterminated block comment")))
        else
          skipWhitespaceAndComments()

      case _ => None

  private def isAtEnd(): Boolean = pos >= source.length
  private def peek(): Char = if isAtEnd() then '\u0000' else source.charAt(pos)
  private def peekNext(): Char = if pos + 1 >= source.length then '\u0000' else source.charAt(pos + 1)

  private def eatToken(): Char =
    val c = source.charAt(pos)
    pos += 1
    col += 1
    c

  private def isAlpha(c: Char): Boolean = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
  private def isAlphaNumeric(c: Char): Boolean = isAlpha(c) || c.isDigit

  private def makeToken(tpe: TokenType, lexeme: String, startCol: Int, value: Option[Long] = None): Token =
    Token(tpe, lexeme, line, startCol, value)