class CodeGen:
  var hasErrors = false

  private case class VarInfo(mut: String, ptr: String)

  private val sb = new StringBuilder       
  private val allocas = new StringBuilder   
  private var tmpCounter = 0
  private var labelCounter = 0

  private var scopes: List[Map[String, VarInfo]] = List(Map())
  private var loops: List[(String, String)] = Nil   

  private var terminated = false   
  private var curBlock = "entry"  

  private def error(n: ASTNode, msg: String): Unit =
    hasErrors = true
    System.err.println(s"Error: line ${n.line}, column ${n.column}: $msg")

  private def fresh(): String =
    tmpCounter += 1
    s"%t.$tmpCounter"

  private def newLabel(prefix: String): String =
    labelCounter += 1
    s"$prefix.$labelCounter"

  private def emit(s: String): Unit =
    sb.append("  ").append(s).append('\n')

  private def startBlock(label: String): Unit =
    sb.append(s"$label:\n")
    curBlock = label
    terminated = false

  private def branch(target: String): Unit =
    if !terminated then
      emit(s"br label %$target")
      terminated = true

  private def condBranch(c: String, t: String, f: String): Unit =
    emit(s"br i1 $c, label %$t, label %$f")
    terminated = true

  private def lookup(name: String): Option[VarInfo] =
    scopes.iterator.flatMap(_.get(name)).nextOption()

  private def withScope(body: => Unit): Unit =
    scopes = Map[String, VarInfo]() :: scopes
    body
    scopes = scopes.tail

  private def scoped(n: ASTNode): Unit = n match
    case _: Block => stmt(n)
    case _        => withScope(stmt(n))

  def generate(program: Program): String =
    program.elems.foreach(stmt)
    if !terminated then emit("ret i64 0")

    val out = new StringBuilder
    out.append("; ModuleID = 'spl'\n")
    out.append("source_filename = \"spl\"\n")
    out.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128\"\n")
    out.append("target triple = \"x86_64-unknown-linux-gnu\"\n\n")
    out.append("define i64 @main() {\nentry:\n")
    out.append(allocas)
    out.append(sb)
    out.append("}\n")
    out.toString

  private def stmt(n: ASTNode): Unit =
    if terminated then startBlock(newLabel("dead"))

    n match
      case d: Declare =>
        val name = d.ident match
          case Ident(nm, _, _) => nm
          case other =>
            error(other, "invalid declaration target")
            ""
        val v = expr(d.expr)  
        if name.nonEmpty then
          if scopes.head.contains(name) then
            error(d, s"variable '$name' is already declared")
          else
            tmpCounter += 1
            val ptr = s"%v.$name.$tmpCounter"
            scopes = scopes.head.updated(name, VarInfo(d.mut, ptr)) :: scopes.tail
            allocas.append(s"  $ptr = alloca i64, align 8\n")
            emit(s"store i64 $v, ptr $ptr, align 8")

      case a: Assign =>
        val v = expr(a.expr)
        a.ident match
          case Ident(name, _, _) =>
            lookup(name) match
              case None =>
                error(a, s"assignment to undeclared variable '$name'")
              case Some(VarInfo("val", _)) =>
                error(a, s"cannot assign to immutable variable '$name'")
              case Some(info) =>
                emit(s"store i64 $v, ptr ${info.ptr}, align 8")
          case other =>
            error(other, "invalid assignment target")

      case r: Return =>
        val v = expr(r.expr)
        emit(s"ret i64 $v")
        terminated = true

      case Block(stmts, _, _) =>
        withScope(stmts.foreach(stmt))

      case i: If    => genIf(i)
      case w: While => genWhile(w)

      case b: Break =>
        loops match
          case (_, brk) :: _ => branch(brk)
          case Nil           => error(b, "'break' outside of a loop")

      case c: Continue =>
        loops match
          case (cont, _) :: _ => branch(cont)
          case Nil            => error(c, "'continue' outside of a loop")

      case other =>
        expr(other)
        ()

  private def genIf(i: If): Unit =
    val c = cond(i.cond)
    val thenL = newLabel("if.then")
    val endL  = newLabel("if.end")
    val elseL = if i.elseBody.isDefined then newLabel("if.else") else endL

    condBranch(c, thenL, elseL)

    startBlock(thenL)
    scoped(i.thenBody)
    branch(endL)

    i.elseBody.foreach { e =>
      startBlock(elseL)
      scoped(e)
      branch(endL)
    }

    startBlock(endL)

  private def genWhile(w: While): Unit =
    val condL = newLabel("while.cond")
    val bodyL = newLabel("while.body")
    val endL  = newLabel("while.end")

    branch(condL)
    startBlock(condL)
    val c = cond(w.cond)
    condBranch(c, bodyL, endL)

    startBlock(bodyL)
    loops = (condL, endL) :: loops
    scoped(w.body)
    loops = loops.tail
    branch(condL)        

    startBlock(endL) 

  private def cond(e: ASTNode): String =
    val v = expr(e)
    val t = fresh()
    emit(s"$t = icmp ne i64 $v, 0")
    t

  private def expr(n: ASTNode): String = n match
    case IntLiteral(value, _, _) =>
      value

    case BoolLiteral(value, _, _) =>
      if value then "1" else "0"

    case Ident(name, _, _) =>
      lookup(name) match
        case Some(info) =>
          val t = fresh()
          emit(s"$t = load i64, ptr ${info.ptr}, align 8")
          t
        case None =>
          error(n, s"undefined variable '$name'")
          "0"

    case UnaryOp("-", e, _, _) =>
      val v = expr(e)
      val t = fresh()
      emit(s"$t = sub i64 0, $v")
      t

    case UnaryOp("!", e, _, _) =>
      val v = expr(e)
      val c = fresh()
      emit(s"$c = icmp eq i64 $v, 0")
      val t = fresh()
      emit(s"$t = zext i1 $c to i64")
      t

    case UnaryOp(op, _, _, _) =>
      error(n, s"unknown unary operator '$op'")
      "0"

    case BinOp("&&", l, r, _, _) =>
      val a = expr(l)
      val lc = fresh()
      emit(s"$lc = icmp ne i64 $a, 0")
      val rhsL = newLabel("and.rhs")
      val endL = newLabel("and.end")
      val lhsBlock = curBlock
      condBranch(lc, rhsL, endL)

      startBlock(rhsL)
      val b = expr(r)
      val rc = fresh()
      emit(s"$rc = icmp ne i64 $b, 0")
      val rz = fresh()
      emit(s"$rz = zext i1 $rc to i64")
      val rhsEnd = curBlock
      branch(endL)

      startBlock(endL)
      val t = fresh()
      emit(s"$t = phi i64 [ 0, %$lhsBlock ], [ $rz, %$rhsEnd ]")
      t

    case BinOp("||", l, r, _, _) =>
      val a = expr(l)
      val lc = fresh()
      emit(s"$lc = icmp ne i64 $a, 0")
      val rhsL = newLabel("or.rhs")
      val endL = newLabel("or.end")
      val lhsBlock = curBlock
      condBranch(lc, endL, rhsL)

      startBlock(rhsL)
      val b = expr(r)
      val rc = fresh()
      emit(s"$rc = icmp ne i64 $b, 0")
      val rz = fresh()
      emit(s"$rz = zext i1 $rc to i64")
      val rhsEnd = curBlock
      branch(endL)

      startBlock(endL)
      val t = fresh()
      emit(s"$t = phi i64 [ 1, %$lhsBlock ], [ $rz, %$rhsEnd ]")
      t

    case BinOp(op, l, r, _, _) =>
      val a = expr(l)
      val b = expr(r)
      val cmp = op match
        case "==" => Some("eq")
        case "!=" => Some("ne")
        case "<"  => Some("slt")
        case ">"  => Some("sgt")
        case "<=" => Some("sle")
        case ">=" => Some("sge")
        case _    => None

      cmp match
        case Some(pred) =>
          val c = fresh()
          emit(s"$c = icmp $pred i64 $a, $b")
          val t = fresh()
          emit(s"$t = zext i1 $c to i64")
          t
        case None =>
          val instr = op match
            case "+" => "add"
            case "-" => "sub"
            case "*" => "mul"
            case "/" => "sdiv"
            case _ =>
              error(n, s"unknown operator '$op'")
              "add"
          val t = fresh()
          emit(s"$t = $instr i64 $a, $b")
          t

    case _ =>
      error(n, s"cannot generate code for node '${n.kind}'")
      "0"