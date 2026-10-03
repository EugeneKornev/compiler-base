class Parser(tokens: List[Token]):
  var hasErrors = false
  private var pos = 0
  private var symbols = Map[String, String]()
  
  private def current: Token = 
    if pos < tokens.size then tokens(pos) else tokens.last
    
  private def advance(): Token =
    val t = current
    if pos < tokens.size - 1 then pos += 1
    t
    
  private def consume(expectedKinds: String*): Token =
    val t = current
    if expectedKinds.contains(t.tokenType.kind) then
      advance()
    else
      hasErrors = true 
      Token(TokenType.ERROR, "", t.line, t.column)

  def parseProgram(): Program =
    val line = current.line
    val col = current.column
    var statements = List[ASTNode]()
    
    while current.tokenType.kind != "EOF" do
      val startPos = pos
      statements = statements :+ parseStatement()
      if pos == startPos then advance()
      
    if statements.isEmpty || statements.last.kind != "Return" then
      hasErrors = true
      
    Program(statements, line, col)

  private def parseStatement(): ASTNode =
    current.tokenType.kind match
      case "VAR" | "VAL" => parseDeclare()
      case "RETURN"      => parseReturn()
      case "IDENT" => 
        val next = if pos + 1 < tokens.size then tokens(pos + 1) else tokens.last
        if next.tokenType.kind == "ASSIGN" then
          parseAssign()
        else
          parseExprStmt()
      case _ => 
        parseExprStmt()

  private def parseDeclare(): ASTNode =
    val mutToken = advance()
    val mut = if mutToken.tokenType.kind == "VAR" then "var" else "val"
    val idToken = consume("IDENT")

    if symbols.contains(idToken.lexeme) then hasErrors = true
    else symbols += (idToken.lexeme -> mut)

    val ident = Ident(idToken.lexeme, idToken.line, idToken.column)
    
    if current.tokenType.kind != "ASSIGN" then
      hasErrors = true
      if current.tokenType.kind == "SEMI" then
        val errLine = current.line
        val errCol = current.column
        advance() 
        
        if current.tokenType.kind == "RETURN" then
          advance() 
          val error = ErrorNode(current.line, current.column)
          parseExpression() 
          consume("SEMI") 
          return Declare(mut, ident, error, mutToken.line, mutToken.column)
          
        return Declare(mut, ident, ErrorNode(errLine, errCol), mutToken.line, mutToken.column)
      
      val error = ErrorNode(current.line, current.column)
      while current.tokenType.kind != "SEMI" && current.tokenType.kind != "EOF" do advance()
      if current.tokenType.kind == "SEMI" then advance()
      return Declare(mut, ident, error, mutToken.line, mutToken.column)

    consume("ASSIGN")
    
    if current.tokenType.kind == "SEMI" || current.tokenType.kind == "EOF" then
      hasErrors = true
      val error = ErrorNode(current.line, current.column)
      if current.tokenType.kind == "SEMI" then advance()
      return Declare(mut, ident, error, mutToken.line, mutToken.column)

    val expr = parseExpression()
    consume("SEMI")
    Declare(mut, ident, expr, mutToken.line, mutToken.column)

  private def parseAssign(): ASTNode =
    val idToken = consume("IDENT")
    
    symbols.get(idToken.lexeme) match
      case None => hasErrors = true
      case Some("val") => hasErrors = true
      case _ =>
      
    val ident = Ident(idToken.lexeme, idToken.line, idToken.column)
    consume("ASSIGN")
    val expr = parseExpression()
    consume("SEMI")
    Assign(ident, expr, idToken.line, idToken.column)

  private def parseReturn(): ASTNode =
    val retToken = consume("RETURN")

    if current.tokenType.kind == "SEMI" then
      hasErrors = true
      val error = ErrorNode(current.line, current.column + 1)
      advance()
      Return(error, retToken.line, retToken.column)
    else
      val expr = parseExpression()
      consume("SEMI")
      Return(expr, retToken.line, retToken.column)

  private def parseExprStmt(): ASTNode =
    val expr = parseExpression()
    consume("SEMI")
    expr

  private def parseExpression(): ASTNode = parseAdditive()

  private def parseAdditive(): ASTNode =
    var left = parseMultiplicative()
    while current.tokenType.kind == "PLUS" || current.tokenType.kind == "MINUS" do
      val opToken = advance()
      val right = parseMultiplicative()
      left = BinOp(opToken.lexeme, left, right, opToken.line, opToken.column)
    left

  private def parseMultiplicative(): ASTNode =
    var left = parseUnary()
    while current.tokenType.kind == "MULT" || current.tokenType.kind == "DIV" do
      val opToken = advance()
      val right = parseUnary()
      left = BinOp(opToken.lexeme, left, right, opToken.line, opToken.column)
    left

  private def parseUnary(): ASTNode =
    if current.tokenType.kind == "MINUS" then
      val opToken = advance()
      val expr = parseUnary()
      UnaryOp(opToken.lexeme, expr, opToken.line, opToken.column)
    else
      parsePrimary()

  private def parsePrimary(): ASTNode =
    val t = current
    t.tokenType.kind match
      case "INT" => 
        advance()
        IntLiteral(t.lexeme, t.line, t.column)
      case "IDENT" =>
        advance()
        if !symbols.contains(t.lexeme) then hasErrors = true
        Ident(t.lexeme, t.line, t.column)
      case "LPAREN" =>
        advance()
        val expr = parseExpression()
        consume("RPAREN")
        expr 
      case _ =>
        hasErrors = true
        ErrorNode(t.line, t.column)