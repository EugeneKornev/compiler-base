trait ASTNode:
  def line: Int
  def column: Int
  def kind: String
  def elems: List[ASTNode]

  def extraFieldsStr: String = ""

  def toJson: String =
    val elemsStr = elems.map(_.toJson).mkString(", ")
    s"""{"line": $line, "column": $column, "kind": "$kind"$extraFieldsStr, "elems": [$elemsStr]}"""

case class Program(elems: List[ASTNode], line: Int, column: Int) extends ASTNode:
  def kind = "Program"

case class Declare(mut: String, ident: ASTNode, expr: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "Declare"
  def elems = List(ident, expr)
  override def extraFieldsStr = s""", "mut": "$mut""""

case class Assign(ident: ASTNode, expr: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "Assign"
  def elems = List(ident, expr)

case class Return(expr: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "Return"
  def elems = List(expr)

case class BinOp(op: String, left: ASTNode, right: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "BinOp"
  def elems = List(left, right)
  override def extraFieldsStr = s""", "value": "$op""""

case class UnaryOp(op: String, expr: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "Unary"
  def elems = List(expr)
  override def extraFieldsStr = s""", "value": "$op""""

case class Ident(name: String, line: Int, column: Int) extends ASTNode:
  def kind = "Ident"
  def elems = List()
  override def extraFieldsStr = s""", "value": "$name""""

case class IntLiteral(value: String, line: Int, column: Int) extends ASTNode:
  def kind = "IntLiteral"
  def elems = List()
  override def extraFieldsStr = s""", "value": $value"""

case class BoolLiteral(value: Boolean, line: Int, column: Int) extends ASTNode:
  def kind = "BoolLiteral"
  def elems = List()
  override def extraFieldsStr = s""", "value": $value"""

case class If(cond: ASTNode, thenBody: ASTNode, elseBody: Option[ASTNode], line: Int, column: Int) extends ASTNode:
  def kind = "If"
  def elems = List(cond, thenBody) ++ elseBody.toList

case class While(cond: ASTNode, body: ASTNode, line: Int, column: Int) extends ASTNode:
  def kind = "While"
  def elems = List(cond, body)

case class Break(line: Int, column: Int) extends ASTNode:
  def kind = "Break"
  def elems = List()

case class Continue(line: Int, column: Int) extends ASTNode:
  def kind = "Continue"
  def elems = List()

case class Block(statements: List[ASTNode], line: Int, column: Int) extends ASTNode:
  def kind = "Block"
  def elems = statements

case class ErrorNode(line: Int, column: Int) extends ASTNode:
  def kind = "Error"
  def elems = List()