# Summary
This is a Scanner, parser and symbol table implementation for the C- langugae.

## Build parser
Type "make" in \CMinusParser directory, this will generate executable ProjectMain

## Rebuild parser
Type "make clean" and type "make" again.

## test against source code
Type "java -cp /usr/share/java/cup.jar:. ProjectMain your_source_code.cm" 
    - you can have two command line arguments:
        - '-a' to print the syntax tree
        - '-s' to print the symbol table
        - 'c'  to print the tm code

The directory TestFilesC3 contains 10 .cm files that can be used to test the program

NOTE: you may have to change the cup path depending on your environment

## Acknowledgements 
We used the tiny parser provided in C1-package.tgz and followed the recommended syntax tree structure from the course slides.
