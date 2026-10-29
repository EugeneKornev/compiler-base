class Parser(tokenList: List[Token]):
  var hasErrors = false

  private val tokens: Vector[Token] = tokenList.toVector
  private var pos = 0

  private var scopes: List[Map[String, String]] = List(Map())
  private var loopDepth = 0

  private def current: Token =
    if pos < tokens.size then tokens(pos) else tokens.last

  private def peekKind(offset: Int): String =
    val i = pos + offset
    (if i < tokens.size then tokens(i) else tokens.last).tokenType.kind

  private def at(kind: String): Boolean = current.tokenType.kind == kind

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

  private def synchronize(): Unit =
    while !at("SEMI") && !at("RBRACE") && !at("EOF") do advance()
    if at("SEMI") then advance()

  private def expectSemi(): Unit =
    if at("SEMI") then advance()
    else
      hasErrors = true
      synchronize()

  private def pushScope(): Unit = scopes = Map[String, String]() :: scopes
  private def popScope(): Unit = scopes = scopes.tail

  private def lookup(name: String): Option[String] =
    scopes.iterator.flatMap(_.get(name)).nextOption()

  private def declare(name: String, mut: String): Unit =
    if scopes.head.contains(name) then hasErrors = true
    else scopes = scopes.head.updated(name, mut) :: scopes.tail

  def parseProgram(): Program =
    val line = current.line
    val col = current.column
    var statements = List[ASTNode]()

    while !at("EOF") do
      val startPos = pos
      statements = statements :+ parseStatement()
      if pos == startPos then advance()

    if !statements.lastOption.exists(alwaysReturns) then
      hasErrors = true

    Program(statements, line, col)

  private def alwaysReturns(n: ASTNode): Boolean = n match
    case _: Return => true
    case b: Block  => b.statements.exists(alwaysReturns)
    case i: If     => i.elseBody.exists(e => alwaysReturns(i.thenBody) && alwaysReturns(e))
    case _         => false

  private def parseStatement(): ASTNode =
    current.tokenType.kind match
      case "VAR" | "VAL" => parseDeclare()
      case "RETURN"      => parseReturn()
      case "IF"          => parseIf()
      case "WHILE"       => parseWhile()
      case "BREAK"       => parseBreak()
      case "CONTINUE"    => parseContinue()
      case "LBRACE"      => parseBlock()
      case "IDENT" if peekKind(1) == "ASSIGN" => parseAssign()
      case _             => parseExprStmt()

  private def parseScopedStatement(): ASTNode =
    if at("LBRACE") then parseBlock()
    else
      pushScope()
      val s = parseStatement()
      popScope()
      s

  private def parseBlock(): ASTNode =
    val lb = advance()
    pushScope()
    var stmts = List[ASTNode]()
    while !at("RBRACE") && !at("EOF") do
      val startPos = pos
      stmts = stmts :+ parseStatement()
      if pos == startPos then advance()
    popScope()
    if at("RBRACE") then advance() else hasErrors = true
    Block(stmts, lb.line, lb.column)

  private def parseIf(): ASTNode =
    val ifToken = advance()
    val cond =
      if at("LPAREN") then
        advance()
        val c = parseExpression()
        consume("RPAREN")
        c
      else
        hasErrors = true
        parseExpression()
        val err = ErrorNode(current.line, current.column)
        if at("RPAREN") then advance()
        err
    val thenBody = parseScopedStatement()
    val elseBody =
      if at("ELSE") then
        advance()
        Some(parseScopedStatement())
      else None
    If(cond, thenBody, elseBody, ifToken.line, ifToken.column)

  private def parseWhile(): ASTNode =
    val whileToken = advance()
    consume("LPAREN")
    val cond = parseExpression()
    consume("RPAREN")
    loopDepth += 1
    val body = parseScopedStatement()
    loopDepth -= 1
    While(cond, body, whileToken.line, whileToken.column)

  private def parseBreak(): ASTNode =
    val t = advance()
    if loopDepth == 0 then hasErrors = true
    expectSemi()
    Break(t.line, t.column)

  private def parseContinue(): ASTNode =
    val t = advance()
    if loopDepth == 0 then hasErrors = true
    expectSemi()
    Continue(t.line, t.column)

  private def parseDeclare(): ASTNode =
    val mutToken = advance()
    val mut = if mutToken.tokenType.kind == "VAR" then "var" else "val"
    val idToken = consume("IDENT")
    val ident = Ident(idToken.lexeme, idToken.line, idToken.column)

    def register(): Unit =
      if idToken.lexeme.nonEmpty then declare(idToken.lexeme, mut)

    if !at("ASSIGN") then
      hasErrors = true
      val error = ErrorNode(current.line, current.column)
      synchronize()
      register()
      return Declare(mut, ident, error, mutToken.line, mutToken.column)

    consume("ASSIGN")

    if at("SEMI") || at("RBRACE") || at("EOF") then
      hasErrors = true
      val error = ErrorNode(current.line, current.column)
      if at("SEMI") then advance()
      register()
      return Declare(mut, ident, error, mutToken.line, mutToken.column)

    val expr = parseExpression()
    expectSemi()
    register()
    Declare(mut, ident, expr, mutToken.line, mutToken.column)

  private def parseAssign(): ASTNode =
    val idToken = consume("IDENT")

    lookup(idToken.lexeme) match
      case None        => hasErrors = true
      case Some("val") => hasErrors = true
      case _           =>

    val ident = Ident(idToken.lexeme, idToken.line, idToken.column)
    val eqToken = consume("ASSIGN")
    val expr = parseExpression()
    expectSemi()
    Assign(ident, expr, eqToken.line, eqToken.column)

  private def parseReturn(): ASTNode =
    val retToken = consume("RETURN")

    if at("SEMI") then
      hasErrors = true
      val error = ErrorNode(current.line, current.column + 1)
      advance()
      Return(error, retToken.line, retToken.column)
    else
      val expr = parseExpression()
      expectSemi()
      Return(expr, retToken.line, retToken.column)

  private def parseExprStmt(): ASTNode =
    val expr = parseExpression()
    expectSemi()
    expr

  private def parseExpression(): ASTNode = parseOr()

  private def leftAssoc(ops: Set[String])(operand: () => ASTNode): ASTNode =
    var left = operand()
    while ops.contains(current.tokenType.kind) do
      val opToken = advance()
      val right = operand()
      left = BinOp(opToken.lexeme, left, right, opToken.line, opToken.column)
    left

  private def parseOr(): ASTNode =
    leftAssoc(Set("OR"))(() => parseAnd())

  private def parseAnd(): ASTNode =
    leftAssoc(Set("AND"))(() => parseEquality())

  private def parseEquality(): ASTNode =
    leftAssoc(Set("EQ", "NE"))(() => parseRelational())

  private def parseRelational(): ASTNode =
    leftAssoc(Set("LT", "GT", "LE", "GE"))(() => parseAdditive())

  private def parseAdditive(): ASTNode =
    leftAssoc(Set("PLUS", "MINUS"))(() => parseMultiplicative())

  private def parseMultiplicative(): ASTNode =
    leftAssoc(Set("MULT", "DIV"))(() => parseUnary())

  private def parseUnary(): ASTNode =
    if at("NOT") || at("MINUS") then
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
      case "TRUE" =>
        advance()
        BoolLiteral(true, t.line, t.column)
      case "FALSE" =>
        advance()
        BoolLiteral(false, t.line, t.column)
      case "IDENT" =>
        advance()
        if lookup(t.lexeme).isEmpty then hasErrors = true
        Ident(t.lexeme, t.line, t.column)
      case "LPAREN" =>
        advance()
        val expr = parseExpression()
        if at("RPAREN") then
          advance()
          expr
        else
          hasErrors = true
          ErrorNode(current.line, current.column)
      case _ =>
        hasErrors = true
        ErrorNode(t.line, t.column)