/*
  This file contains all of the logic required to generate the intermediate assembly 
  code from the generated AST
*/
import c1Absyn.*;

public class CodeGenerator implements AbsynVisitor{
    private static final int pc = 7;
    private static final int gp = 6;
    private static final int fp = 5;
    private static final int ac = 0;
    private static final int ac1 = 1;
    private static final int ofpFO = 0;
    private static final int retFO = -1;
    private static final int initFO = -2;

    private static int mainEntry = 0;
    private static int globalOffset = 0;
    private static int frameOffset = 0;
    private static int emitLoc = 0;
    private static int highEmitLoc = 0;

    public CodeGenerator() {}

    // functions for emit routines 
    private void emitRO(String op, int r, int s, int t, String c) {
        System.out.printf("%3d: %5s %d,%d,%d", emitLoc, op, r, s, t);
        System.out.printf("\t%s\n", c);
        ++emitLoc;

        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    private void emitRM(String op, int r, int d, int s, String c) {
        System.out.printf("%3d: %5s %d,%d(%d)", emitLoc, op, r, d, s);
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        
        if (highEmitLoc < emitLoc){
            highEmitLoc = emitLoc;
        }
    }

    private void emitRMAbs(String op, int r, int a, String c) {
        // System.err.println("EMIT: " + emitLoc + " MAIN: " + mainEntry + " a: " + a + " MESS: " + c);
        System.out.printf("%3d: %5s %d,%d(%d)", emitLoc, op, r, a - (emitLoc + 1), pc);
        System.out.printf("\t%s\n", c);
        ++emitLoc;
        
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    private int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;

        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc; 

        return i;
    }

    private void emitBackup(int loc) {
        if (loc > highEmitLoc) emitComment("BUG in emitBackup");

        emitLoc = loc;
    }

    private void emitRestore() {
        emitLoc = highEmitLoc;
    }

    private void emitComment(String c) {
        System.out.printf("* %s\n", c);
    }
    
    // function to set-up standard prelude code (include code for I/O functions)
    private void preludeCode() {
        emitComment("Start of assembly code generation");
        emitComment("Standard prelude");
        emitRM("LD", gp, 0, ac, "load gp with maxaddress");
        emitRM("LDA", fp, 0, gp, "copy gp to fp");
        emitRM("ST", ac, 0, ac, "clear value at location 0");

        int savedLoc = emitSkip(1);

        //pc = 7; gp = 6; fp = 5; ac = 0; ac1 = 1; ofpFO = 0; retFO = -1; initFO = -2;
        emitComment("Jump around i/o routines");
        emitComment("Code for input routine");
        emitRM("ST", ac, retFO, fp, "store input return");
        emitRO("IN", ac, 0, 0, "input");
        emitRM("LD", pc, retFO, fp, "return to caller");

        emitComment("Code for output routine");
        emitRM("ST", ac, retFO, fp, "store output return");
        emitRM("LD", ac, initFO, fp, "load output value");
        emitRO("OUT", ac, 0, 0, "output");
        emitRM("LD", pc, retFO, fp, "return to caller");

        int savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitRMAbs("LDA", pc, savedLoc2, "");
        emitRestore();
        emitComment("End of standard prelude");
    }

    // function to write standard finale code
    private void finaleCode() {
        emitComment("Standard finale");
        emitRM("ST", fp, globalOffset + ofpFO, fp, "push ofp");
        emitRM("LDA", fp, globalOffset, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRMAbs("LDA", pc, mainEntry, "jump to main loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");

        emitComment("End of execution");
        emitRO("HALT", 0,0,0, "");
    }

    public void visit(Absyn trees) { // wrapper for post-order traversal
        // generate the prelude ... 
        preludeCode();
       
        // make a request to the visit method for DecList
        visit((DecList) trees, 0, false);
        
        // generate finale
        finaleCode();

    }
        
    public void visit( DecList dec, int level, boolean isAddr ){
        while (dec != null) {
            if (dec.head != null){
                dec.head.accept(this, level, false);
            }
            dec = dec.tail;
        }
    }

    public void visit( VarDecList var, int level, boolean isAddr ){
        while (var != null){
            if(var.head != null){
                var.head.accept(this, level, false);

                int size = 1;

                if(var.head instanceof ArrayDec)
                    size += (((ArrayDec)var.head).size);
                

                level -= size;
                
                if(var.head.nestLevel ==  0){
                    globalOffset -= size;
                }
            }
            
            var = var.tail;
        }

        frameOffset = level;
    }

    public void visit( ExpList exp, int level, boolean isAddr ){
        while (exp != null){
            if(exp.head != null){
                exp.head.accept(this, level, false);
            }
            exp = exp.tail;
        }
    }

    public void visit( CompoundExp exp, int level, boolean isAddr ){
        emitComment("-> CompoundExp");

        if (exp.decs != null) {
            // System.err.println("OFFSET: " + globalOffset);
            exp.decs.accept(this, level + globalOffset, false);
            
            level = frameOffset;
        }

        if (exp.exps != null)
            exp.exps.accept(this, level, false);

        emitComment("<- CompoundExp");
    }

    public void visit( ReturnExp exp, int level, boolean isAddr ){ //change maybe
        emitComment("-> return");

        //return expression not an address
        if (exp.exp != null){
            exp.exp.accept(this, level - 1, false);
        }

        emitRM("LD", pc, retFO, fp, "return to caller");

        emitComment("<- return");
    }

    public void visit( WhileExp exp, int level, boolean isAddr ){
        emitComment("-> while");
        emitComment("while: jump after body comes back here");

        //save initial location
        int savedLoc = emitSkip(0);

        if (exp.test != null)
            exp.test.accept(this, level, false);

        emitComment("while: jump to end belongs here");

        //save location after test
        int savedLoc2 = emitSkip(1);

        if (exp.body != null)
            exp.body.accept(this, level - 1, false);

        emitRMAbs("LDA", pc, savedLoc, "while: absolute jmp to test");

        //save location after body
        int savedLoc3 = emitSkip(0);

        emitBackup(savedLoc2);
        emitRMAbs("JEQ", ac, savedLoc3, "while: jmp to end");
        emitRestore();

        emitComment("<- while");

    }

    public void visit( IfExp exp, int level, boolean isAddr ){
        emitComment("-> if");

        //process condition
        exp.test.accept( this, level, false);
        int savedLoc = emitSkip(1);

        exp.thenpart.accept( this, level - 1 , false);
        int savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitComment("if: jump to end belongs here");
        emitRMAbs("JEQ", ac, savedLoc2 + 1, "if: jmp to else");
        emitRestore();

        emitComment("if: jump to else belongs here");

        if(!(exp.elsepart instanceof NilExp)){
            emitLoc++;
            exp.elsepart.accept( this, level - 1, false);
            emitLoc--;
        }
       
        int savedLoc3 = emitSkip(1);
        //save location after body
        emitBackup(savedLoc2);
        emitRMAbs("LDA", pc, savedLoc3 + 1, "if: jmp to end");
        emitRestore();
        
        emitComment("<- if");
    }

    public void visit( AssignExp exp, int level, boolean isAddr ){
        emitComment("-> AssignExp");
        int offsetPointer = fp;

        // left hand side will be an address
        if(exp.lhs != null){
            if(exp.lhs.variable instanceof IndexVar){
                IndexVar var = ((IndexVar)exp.lhs.variable);
                if(var.dec.nestLevel == 0){
                    offsetPointer = gp;
                }
            }
            exp.lhs.accept(this, level - 1, true);
        }

        // right hand side will be a value
        exp.rhs.accept(this, level - 2, false);

        emitRM("LD", ac1, level - 1, offsetPointer, "VAR: load left");
        // emitRM("LD", ac1, level - 2, fp, "VAR: load right");
        emitRM("ST", ac, ac, ac1, "");
        // emitRM("ST", ac1, level, fp, "assign: store value");

        emitComment("<- AssignExp");
    }

    public void visit( OpExp exp, int level, boolean isAddr ){
        emitComment("-> op");

        // Left side op handling
		if(exp.left instanceof VarExp){
			VarExp v = (VarExp)exp.left;

			if(v.variable instanceof SimpleVar){
				v.accept(this, level, false);
				emitRM("ST", ac, level--, fp, "Op: Push left");
			} else {
                System.err.println("HERE");
                v.accept(this, level, true);
			}
        } else if(exp.left instanceof OpExp){
			exp.left.accept(this, level, false);
			emitRM("ST", ac, level--, fp, "OpExp");
        } else if(exp.left instanceof BoolExp){
            exp.left.accept(this, level, false);
			emitRM("ST", ac, level--, fp, "Op: Push left");
        } else if(exp.left instanceof IntExp){
            exp.left.accept(this, level, false);
			emitRM("ST", ac, level--, fp, "Op: Push left");
        } else {
            exp.left.accept(this, level, false);
        }
		
        // Right side op handling
		if(exp.right instanceof VarExp){
			VarExp v = (VarExp)exp.right;

			if(v.variable instanceof SimpleVar){
                v.accept(this, level, false);
			} else {
                v.accept(this, level, false);
			}
		
		} else {
            exp.right.accept(this, level, false);
        }

        emitRM("LD", ac1, ++level, fp, "Op: Load left");

        if (exp.op == OpExp.PLUS)
            emitRO("ADD", ac, ac1, ac, "op +");
        else if (exp.op == OpExp.MINUS)
            emitRO("SUB", ac, ac1, ac, "op -");
        else if (exp.op == OpExp.TIMES)
            emitRO("MUL", ac, ac1, ac, "op *");
        else if (exp.op == OpExp.DIV)
            emitRO("DIV", ac, ac1, ac, "op /");
        else
        {
            if (exp.op == OpExp.LT)
            {
                emitRO("SUB", ac, ac1, ac, "op <");
                emitRM("JLT", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.LTE)
            {
                emitRO("SUB", ac, ac1, ac, "op <=");
                emitRM("JLE", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.GT)
            {
                emitRO("SUB", ac, ac1, ac, "op >");
                emitRM("JGT", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.GTE)
            {
                emitRO("SUB", ac, ac1, ac, "op >=");
                emitRM("JGE", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.EQ)
            {
                emitRO("SUB", ac, ac1, ac, "op ==");
                emitRM("JEQ", ac, 2, pc, "br if true");
            }
            else if (exp.op == OpExp.NEQ)
            {
                emitRO("SUB", ac, ac1, ac, "op !=");
                emitRM("JNE", ac, 2, pc, "br if true");
            }
            emitRM("LDC", ac, ac, ac, "false case");
            emitRM("LDA", pc, 1, pc, "unconditional jmp");
            emitRM("LDC", ac, 1, ac, "true case");
        }

        emitComment("<- op");

    }

    public void visit( CallExp exp, int level, boolean isAddr ){
        emitComment("-> call of function: " + exp.func);
        level--;
        
        int argCount = 0;
        while(exp.args != null){
            if(exp.args.head != null){
                if((exp.args.head instanceof VarExp)){
                    VarExp var = (VarExp)exp.args.head;
                    if(var.variable.dec instanceof ArrayDec){
                        var.variable.accept(this, level + initFO, true);
                    } else {
                        var.variable.accept(this, level + initFO, false);
                    }
                } else {
                    exp.args.head.accept(this, level + initFO, false);
                }
                
                level--;
                argCount++;

                emitRM("ST", ac, level - 1, fp, "call arg"); 
            
            }
            exp.args = exp.args.tail;
        }

        level = level + argCount;

        //store fp
        emitRM("ST", fp, level+ofpFO, fp, "push ofp");
        emitRM("LDA", fp, level, fp, "push frame");
        emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
        emitRMAbs("LDA", pc, ((FunctionDec)exp.dtype).funAddr, "jump to fun loc");
        emitRM("LD", fp, ofpFO, fp, "pop frame");

        emitComment("<- call");
    }

    public void visit( VarExp exp, int level, boolean isAddr ){
        if (exp.variable != null)
            exp.variable.accept(this, level, isAddr);
    }

    public void visit( BoolExp exp, int level, boolean isAddr ){
        emitComment("-> BoolExp");

        // convert truth value to int: 1 = true, 0 = false
        int convertedBool = exp.value.toLowerCase().equals("true") ? 1 : 0;

        emitRM("LDC", ac, convertedBool, ac, "load boolean value");
        emitRM("ST", ac, level, fp, "");

        emitComment("<- BoolExp (end)");
    }

    public void visit( IntExp exp, int level, boolean isAddr ){
        emitComment("-> IntExp code gen");

        emitRM("LDC", ac, Integer.parseInt(exp.value), ac, "load constant value");
        emitRM("ST", ac, level - 1, fp, "");

        emitComment("<- end of IntExp code gen");
    }

    public void visit( NilExp exp, int level, boolean isAddr ){

    }

    public void visit( SimpleVar var, int level, boolean isAddr ){
        emitComment("-> SimpleVar code gen");
        emitComment("looking up ID: " + var.name);
     
        int offsetPointer = var.dec.nestLevel == 0 ? gp : fp; // need to use different offset if var is global vs local

        // var is being used on the left side of an exp
        if (isAddr) {
            // System.err.println("ADD VARNAME: " + var.name + " OFFSET: " + var.dec.offset + " LEVEL: " + level);
            emitRM("LDA", ac, var.dec.offset, offsetPointer, "load address");
            emitRM("ST", ac, level, fp, "store address");
        }else {
            // System.err.println("NOT ADD VARNAME: " + var.name + " OFFSET: " + var.dec.offset + " LEVEL: " + level);
            emitRM("LD", ac, var.dec.offset, offsetPointer, "load var value");
            emitRM("ST", ac, level, fp, "store var value");
        }

        emitComment("<- end of SimpleVar code gen");
    }

    public void visit( IndexVar var, int level, boolean isAddr ){
        emitComment("-> IndexVar");
        emitComment("Looking up ID: " + var.name);

        if (var.dec == null) {
            System.err.println("Array: " + var.name + " has no dec?");
        }

        int offsetPointer = var.dec.nestLevel == 0 ? gp : fp;
        int baseAddress = var.dec.offset;
        // int arraySize = var.dec.size; 
        // System.err.println("LOOKING FOR ARRAY: " + var.name + " BASE = " + baseAddress + " SIZE = " + arraySize + " NEST = " + var.dec.nestLevel);
        // emitRM("LD", 0, (baseAddress - 1), offsetPointer, "load array size (indexVar)");
        
        // System.err.println("INDEX DEC = " + var.index.dtype);
        var.index.accept(this, level, false);

        emitRM("LDA", ac1, baseAddress, offsetPointer, "Load base array address");
        emitRO("ADD", ac, ac, ac1, "calculate offset of index location");

        if (isAddr) {
            System.err.println("BASE = " + baseAddress);
            emitRO("ST", ac, level, offsetPointer, "store calculated index location"); // store calculate memory spot
            
        // not on the left side, so we want the value instead of address
        }else {
            emitRM("LD", ac, 0, ac, "load value from index location");
            emitRM("ST", ac, level, fp, "store array value");
        }

        emitComment("<- IndexVar (end)");

    }

    public void visit( FunctionDec dec, int level, boolean isAddr ){

        emitComment("Processing function: " + dec.name);
        emitComment("jump around function body here");

        int savedLoc = emitSkip(1);

        //check if entering main functon
        if (dec.name.equalsIgnoreCase("main")) 
            mainEntry = emitLoc;

        dec.funAddr = emitLoc;
    
        emitRM("ST", ac, retFO, fp, "store return");

        int savedLoc2;
        int args = 0;
        VarDecList params = dec.params;

        level += initFO;

        int paramLevel = level;
        while (dec.params != null){
            if(dec.params.head != null){
                if((dec.params.head instanceof ArrayDec)){
                    ArrayDec arr = (ArrayDec)dec.params.head;
                    arr.nestLevel = 0;
                    arr.offset = globalOffset;
                } else {
                    dec.params.head.accept(this, paramLevel, false);
                }    
            }
            paramLevel--;
            dec.params = dec.params.tail;
        }
           

        //count number of function parameters
        while (params != null) {
            if (params.head != null)
                args++;

            params = params.tail;
        }

        if (dec.body != null)
            dec.body.accept(this, (level - args), false);

        emitRM("LD", pc, retFO, fp, "return to caller");

        savedLoc2 = emitSkip(0);

        emitBackup(savedLoc);
        emitRMAbs("LDA", pc, savedLoc2, "jump around fn body");
        emitRestore();
        emitComment("Leaving function: " + dec.name);
    }

    public void visit( SimpleDec dec, int level, boolean isAddr ){
        if(dec.name.equalsIgnoreCase("VOID(function arg)"))
            return;

        if (dec.nestLevel == 0) {
            emitComment("allocating global var: " + dec.name);
            dec.offset = globalOffset;
            globalOffset--;
        }
        else {
            emitComment("processing local var: " + dec.name);
            dec.offset = level;
        }
    }

    public void visit( ArrayDec dec, int level, boolean isAddr ){
        int memoryNeeded = dec.size; // size of block needed = num elements + 1 for storing the size itself
        int offsetPointer = dec.nestLevel == 0 ? gp : fp;
        // System.err.println("-------- Processing ARRAY: " + dec.name);

        // global 
        if (dec.nestLevel == 0) {
            emitComment("allocating global array: " + dec.name);
            dec.offset = globalOffset - dec.size;
            //System.err.println("GLOBAL OFFSET (before alloc) = " + globalOffset);
            globalOffset -= memoryNeeded;
            //System.err.println("GLOBAL OFFSET (after alloc) = " + globalOffset);
        }
        // local
        else {
            emitComment("processing local array: " + dec.name);
            dec.offset = frameOffset - dec.size;
            //System.err.println("FRAME OFFSET (before alloc) = " + frameOffset);
            frameOffset -= memoryNeeded;
            // System.err.println("FRAME OFFSET (after alloc) = " + frameOffset);
        }

        // store size right under base address
        emitRM("LDC", ac, dec.size, ac, "load array size");
        emitRM("ST", ac, dec.offset - 1, offsetPointer, "store size of array");
        // System.err.println("location of ARRAY in mem = " + dec.offset);
        // System.err.println("location of SIZE in mem = " + (dec.offset - 1));
        // System.err.println("ARR: " + dec.offset);

    }

    public void visit( NameTy type, int level, boolean isAddr ){
        
    }
}