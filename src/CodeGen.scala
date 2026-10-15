class CodeGen:
  var hasErrors = false

  private val sb = new StringBuilder
  private var tmpCounter = 0
  private var deadCounter = 0
  private var vars = Map[String, String]() 
  private var terminated = false         

  private def error(n: ASTNode, msg: String): Unit =
    hasErrors = true
    System.err.println(s"Error: line ${n.line}, column ${n.column}: $msg")

  private def fresh(): String =
    tmpCounter += 1
    s"%t.$tmpCounter"

  private def emit(s: String): Unit =
    sb.append("  ").append(s).append('\n')

  def generate(program: Program): String =
    sb.append("; ModuleID = 'spl'\n")
    sb.append("source_filename = \"spl\"\n\n")
    sb.append("define i64 @main() {\nentry:\n")
    program.elems.foreach(stmt)
    if !terminated then emit("ret i64 0")
    sb.append("}\n")
    sb.toString

  private def stmt(n: ASTNode): Unit =
    if terminated then
      deadCounter += 1
      sb.append(s"dead.$deadCounter:\n")
      terminated = false

    n match
      case d: Declare =>
        val name = d.ident match
          case Ident(nm, _, _) => nm
          case other =>
            error(other, "invalid declaration target")
            ""
        val v = expr(d.expr)
        if name.nonEmpty then
          if vars.contains(name) then
            error(d, s"variable '$name' is already declared")
          else
            vars += (name -> d.mut)
            emit(s"%v.$name = alloca i64, align 8")
            emit(s"store i64 $v, ptr %v.$name, align 8")

      case a: Assign =>
        val v = expr(a.expr)
        a.ident match
          case Ident(name, _, _) =>
            vars.get(name) match
              case None =>
                error(a, s"assignment to undeclared variable '$name'")
              case Some("val") =>
                error(a, s"cannot assign to immutable variable '$name'")
              case Some(_) =>
                emit(s"store i64 $v, ptr %v.$name, align 8")
          case other =>
            error(other, "invalid assignment target")

      case r: Return =>
        val v = expr(r.expr)
        emit(s"ret i64 $v")
        terminated = true

      case other =>
        expr(other)
        ()

  private def expr(n: ASTNode): String = n match
    case IntLiteral(value, _, _) =>
      value

    case Ident(name, _, _) =>
      if vars.contains(name) then
        val t = fresh()
        emit(s"$t = load i64, ptr %v.$name, align 8")
        t
      else
        error(n, s"undefined variable '$name'")
        "0"

    case UnaryOp(_, e, _, _) =>
      val v = expr(e)
      val t = fresh()
      emit(s"$t = sub i64 0, $v")
      t

    case BinOp(op, l, r, _, _) =>
      val a = expr(l)
      val b = expr(r)
      val t = fresh()
      val instr = op match
        case "+" => "add"
        case "-" => "sub"
        case "*" => "mul"
        case "/" => "sdiv"
        case _ =>
          error(n, s"unknown operator '$op'")
          "add"
      emit(s"$t = $instr i64 $a, $b")
      t

    case _ =>
      error(n, s"cannot generate code for node '${n.kind}'")
      "0"