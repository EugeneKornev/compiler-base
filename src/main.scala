import scala.io.Source
import scala.util.Using
import java.io.{File, PrintWriter}

object Main:
  def main(args: Array[String]): Unit =
    if args.isEmpty then
      System.err.println("Usage: syspro-compiler [--lex] <file.spl> [-o <output.json>]")
      sys.exit(1)

    val argsList = args.toList
    
    val inputPath = argsList.dropWhile(_ != "--lex").drop(1).headOption
      .getOrElse(argsList.filterNot(_.startsWith("-")).last)
    
    val outputPath = argsList.dropWhile(_ != "-o").drop(1).headOption

    val sourceCode = Using(Source.fromFile(inputPath)) { source =>
      source.mkString
    }.getOrElse {
      System.err.println(s"Error: Cannot read file $inputPath")
      sys.exit(1)
    }

    val lexer = Lexer(sourceCode)
    val tokens = collection.mutable.ArrayBuffer.empty[Token]

    var t = lexer.nextToken()
    while t.tokenType != TokenType.EOF do
      tokens += t
      t = lexer.nextToken()
      
    tokens += t

    val json = tokens.map(tokenToJson).mkString("[\n  ", ",\n  ", "\n]")
    
    outputPath match
      case Some(path) =>
        val file = new File(path)
        file.getParentFile.mkdirs() 
        val pw = new PrintWriter(file)
        pw.write(json)
        pw.close()
      case None =>
        println(json)

    if lexer.hasErrors then
      sys.exit(1)

  private def tokenToJson(t: Token): String =
    val kindStr = s""""${t.kind}""""
    val lexemeStr = s""""${escapeJson(t.lexeme)}""""
    s"""{"kind": $kindStr, "lexeme": $lexemeStr, "line": ${t.line}, "column": ${t.column}}"""

  private def escapeJson(s: String): String =
    s.replace("\\", "\\\\")
     .replace("\"", "\\\"")
     .replace("\n", "\\n")
     .replace("\r", "\\r")
     .replace("\t", "\\t")