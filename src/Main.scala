import scala.io.Source
import scala.util.Using
import java.io.{File, PrintWriter}

object Main:
  private val Modes = Set("--lex", "--parse")

  private def die(msg: String): Nothing =
    System.err.println(msg)
    sys.exit(1)

  def main(args: Array[String]): Unit =
    val mode = args.find(Modes.contains).getOrElse(
      die("Usage: syspro-compiler [--lex | --parse] <file.spl> [-o <output.json>]"))

    val oIdx = args.indexOf("-o")
    val outputPath: Option[String] =
      if oIdx >= 0 && oIdx + 1 < args.length then Some(args(oIdx + 1)) else None

    val positional = args.indices
      .filter(i => !Modes.contains(args(i)) && args(i) != "-o" && !(oIdx >= 0 && i == oIdx + 1))
      .map(args(_))
      .toList

    val inputPath = positional.headOption.getOrElse(
      die("Error: Could not determine input file from arguments: " + args.mkString(" ")))

    val sourceCode = Using(Source.fromFile(inputPath))(_.mkString)
      .getOrElse(die(s"Error: Cannot read file $inputPath"))

    val lexer = Lexer(sourceCode)
    var tokens = List[Token]()
    var t = lexer.nextToken()
    while t.tokenType != TokenType.EOF do
      tokens = tokens :+ t
      t = lexer.nextToken()
    tokens = tokens :+ t

    mode match
      case "--lex" =>
        writeOut(outputPath, tokens.map(tokenToJson).mkString("[\n  ", ",\n  ", "\n]"))
        if lexer.hasErrors then sys.exit(1)

      case _ => 
        val parser = Parser(tokens)
        val ast = parser.parseProgram()
        writeOut(outputPath, ast.toJson)
        if lexer.hasErrors || parser.hasErrors then sys.exit(1)

  private def writeOut(path: Option[String], content: String): Unit =
    path match
      case Some(p) =>
        val file = new File(p)
        Option(file.getParentFile).foreach(_.mkdirs())
        val pw = new PrintWriter(file)
        try pw.write(content) finally pw.close()
      case None =>
        println(content)

  private def tokenToJson(t: Token): String =
    val kindStr = s""""${t.kind}""""
    val lexemeStr = s""""${escapeJson(t.lexeme)}""""
    s"""{"kind": $kindStr, "value": $lexemeStr, "line": ${t.line}, "column": ${t.column}}"""

  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
     .replace("\"", "\\\"")
     .replace("\n", "\\n")
     .replace("\r", "\\r")
     .replace("\t", "\\t")