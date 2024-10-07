JAVA=java
JAVAC=javac
JFLEX=jflex
# CLASSPATH=-cp /usr/share/java/cup.jar:.
CUP=cup
# JFLEX=~/Projects/jflex/bin/jflex
# CLASSPATH=-cp ~/Projects/java-cup-11b.jar:.
CUP=$(JAVA) java_cup.Main

all: ProjectMain.class

ProjectMain.class: c1Absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java SemanticAnalyzer.java CodeGenerator.java Scanner.java ProjectMain.java

%.class: %.java
	$(JAVAC) $^

Lexer.java: cMinus.flex
	$(JFLEX) cMinus.flex

parser.java: cMinus.cup
	#$(CUP) -dump -expect 3 cMinus.cup
	$(CUP) -expect 3 cMinus.cup

clean:
	rm -f parser.java Lexer.java sym.java TestFilesC2/*.sym TestFilesC2/*.abs TestFilesC3/*.sym TestFilesC3/*.abs TestFilesC3/*.tm  *.class c1Absyn/*.class *~
