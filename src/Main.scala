import scala.io.Source
import scala.util.Using
import scala.sys.process.*
import java.io.{File, IOException, PrintWriter}
import java.nio.file.Files

object Main:
  private val Modes = Set("--lex", "--parse", "--llvm", "--compile")

  private def die(msg: String): Nothing =
    System.err.println(msg)
    sys.exit(1)

  def main(args: Array[String]): Unit =
    val mode = args.find(Modes.contains).getOrElse(
      die("Usage: syspro-compiler [--lex | --parse | --llvm] <file.spl> [-o <output>]\n" +
          "       syspro-compiler --compile <output_exe> <file.spl>"))

    val oIdx = args.indexOf("-o")
    val oValue: Option[String] =
      if oIdx >= 0 && oIdx + 1 < args.length then Some(args(oIdx + 1)) else None

    val positional = args.indices
      .filter(i => !Modes.contains(args(i)) && args(i) != "-o" && !(oIdx >= 0 && i == oIdx + 1))
      .map(args(_))
      .toList

    val (inputPath, outputPath) = (mode, oValue, positional) match
      case ("--compile", None, List(out, in)) => (in, Some(out))
      case ("--compile", Some(out), List(in)) => (in, Some(out))
      case ("--compile", _, _) =>
        die("--compile expects: <output_exe> <file.spl>  (or <file.spl> -o <output_exe>)")
      case (_, out, in :: _) => (in, out)
      case _ =>
        die("Error: Could not determine input file from arguments: " + args.mkString(" "))

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

      case "--parse" =>
        val parser = Parser(tokens)
        val ast = parser.parseProgram()
        writeOut(outputPath, ast.toJson)
        if lexer.hasErrors || parser.hasErrors then sys.exit(1)

      case _ =>
        val parser = Parser(tokens)
        val ast = parser.parseProgram()
        if lexer.hasErrors || parser.hasErrors then sys.exit(1)

        val codegen = CodeGen()
        val ir = codegen.generate(ast)
        if codegen.hasErrors then sys.exit(1)

        if mode == "--llvm" then
          writeOut(outputPath, ir)
        else
          compileToExecutable(ir, outputPath.get)

  private def compileToExecutable(ir: String, exePath: String): Unit =
    Option(File(exePath).getAbsoluteFile.getParentFile).foreach(_.mkdirs())
    val llFile = Files.createTempFile("spl-", ".ll")
    try
      Files.writeString(llFile, ir)
      val cmd = Seq("clang", llFile.toString, "-o", exePath, "-Wno-override-module")
      val rc =
        try cmd.!
        catch case e: IOException => die(s"Error: cannot run clang: ${e.getMessage}")
      if rc != 0 then die(s"Error: clang failed with exit code $rc")
    finally
      Files.deleteIfExists(llFile)

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