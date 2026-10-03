import scala.io.Source
import scala.util.Using
import java.io.{File, PrintWriter}

object Main:
  def main(args: Array[String]): Unit =
    if args.isEmpty then
      System.err.println("Usage: syspro-compiler [--lex | --parse] <file.spl> [-o <output.json>]")
      sys.exit(1)

    var isLex = false
    var isParse = false
    var inputPath = ""
    var outputPath: Option[String] = None

    val argsList = args.toList
    isLex = argsList.contains("--lex")
    isParse = argsList.contains("--parse")

    val outIndex = argsList.indexOf("-o")
    if outIndex != -1 && outIndex + 1 < argsList.length then
      outputPath = Some(argsList(outIndex + 1))

    inputPath = argsList.find { arg =>
      arg != "--lex" && arg != "--parse" && arg != "-o" && Some(arg) != outputPath
    }.getOrElse("")

    if inputPath.isEmpty then
      System.err.println("Error: Could not determine input file from arguments: " + args.mkString(" "))
      sys.exit(1)

    val sourceCode = Using(Source.fromFile(inputPath)) { source =>
      source.mkString
    }.getOrElse {
      System.err.println(s"Error: Cannot read file $inputPath")
      sys.exit(1)
    }

    val lexer = Lexer(sourceCode)
    var tokens = List[Token]()
    
    var t = lexer.nextToken()
    while t.tokenType != TokenType.EOF do
      tokens = tokens :+ t
      t = lexer.nextToken()
    tokens = tokens :+ t 

    if lexer.hasErrors then
      sys.exit(1)

    var parserErrors = false
    val outputContent = if isParse then
      val parser = Parser(tokens)
      val ast = parser.parseProgram()
      parserErrors = parser.hasErrors
      ast.toJson
    else
      tokens.map(tokenToJson).mkString("[\n  ", ",\n  ", "\n]")

    outputPath match
      case Some(path) =>
        val file = new File(path)
        Option(file.getParentFile).foreach(_.mkdirs()) 
        val pw = new PrintWriter(file)
        pw.write(outputContent)
        pw.close()
      case None =>
        println(outputContent)

    if lexer.hasErrors || parserErrors then
      sys.exit(1)

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