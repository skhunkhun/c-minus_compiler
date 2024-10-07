/*
  This is the main java file that is used to run the parser after it is generated 
*/
   
import java.io.*;
import c1Absyn.*;
   
class ProjectMain {
  public static boolean PRINT_TREE = false;
  public static boolean PRINT_SYMBOL = false;
  public static boolean PRINT_CODE = false;
  static public void main(String argv[]) {  

    if (argv.length < 2) {
      System.out.println("ERROR: You must provide command line arguments.");
      System.out.println("Arguments can be:");
      System.out.println("    -a (print syntax tree)");
      System.out.println("    -s (print symbol table tree)");
      System.out.println("    -c (print code generation)");

    }
    else {
      for (String s : argv) {
        if (s.equals("-a"))
          PRINT_TREE = true;
        if (s.equals("-s"))
          PRINT_SYMBOL = true;
        if (s.equals("-c"))
          PRINT_CODE = true;
      }
    }

    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);   
    
      if (PRINT_TREE && result != null) {
        PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".abs")));
        System.setOut(o);
        System.out.println("The abstract syntax tree is:");
        AbsynVisitor visitor = (AbsynVisitor) new ShowTreeVisitor();
        result.accept(visitor, 0, false); 
      }

      if (PRINT_SYMBOL && result != null && !p.syntaxError) {
        PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".sym")));
        System.setOut(o);
        System.out.println("The abstract symbol table tree is:");
        SemanticAnalyzer analyzer = new SemanticAnalyzer((DecList)result, PRINT_SYMBOL);
        analyzer.startSemanticAnalysis();
        if(analyzer.error){
          PRINT_CODE = false;
        }
      }

      if (PRINT_CODE && result != null && !p.syntaxError) {
        PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".tm")));
        System.setOut(o);
        // System.out.println("The code generation is:");
        CodeGenerator code = new CodeGenerator();
        code.visit(result);
      }

    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
