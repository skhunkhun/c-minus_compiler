// Do stuff here

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.jar.Attributes.Name;
import java.util.prefs.NodeChangeEvent;

import c1Absyn.*;
public class SemanticAnalyzer {

    /*
        Key of the symbolTable = int that represents the scope 
        Value of the symbolTable = collection of all symbols for the given scope 

        Key of each value = symbol name 
        Value of each = NodeType representing the symbol
     */
    public HashMap<Integer, HashMap<String, NodeType>> symbolTable; 
    public Stack <String> stack; // use stack to keep track of scope?? call stack.add when entering scope, stack.pop when exiting.

    SemanticVisitor visitor = new SemanticVisitor();

    public DecList program;
    public boolean printSymbolTable;
    public Boolean error = false;

    private int currentScope = -1;
    private int currentReturnType;
    private HashMap<Integer, String> typeNames;

    private enum DecInstance {
        FUNCTION,
        SIMPLE,
        ARRAY,
        UNEXPECTED 
    }

    private ArrayList<String> functionNames = new ArrayList<String>(); // Keep track of functions and blocks for symbol table printing 

    private static final List<Integer> ARITHMETIC_OPS = Arrays.asList(OpExp.PLUS, OpExp.MINUS, OpExp.TIMES, OpExp.DIV);

    private static final List<Integer> COMPARISON_OPS = Arrays.asList(OpExp.EQ, OpExp.NEQ, OpExp.LT, OpExp.LTE, OpExp.GT, OpExp.GTE);

    private static final List<Integer> LOGICAL_OPS = Arrays.asList(OpExp.AND, OpExp.OR, OpExp.NOT);

    public SemanticAnalyzer(DecList program, boolean printSymbolTable) {
        this.program = program;
        this.printSymbolTable = printSymbolTable;
        symbolTable = new HashMap<Integer, HashMap<String, NodeType>>();
        initTypeNamesMap();
    }

    public void startSemanticAnalysis() {
        typeCheck(program);
    }

    private void typeCheck(DecList list) {
        
        setNewScope();

        // add predefined functions (input and output) to symbol table 
        SimpleDec inputParam = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.VOID), "VOID");
        VarDecList inputParams = new VarDecList(inputParam, null);
        NameTy inputReturnType = new NameTy(0, 0, NameTy.INT);
        FunctionDec inputDec = new FunctionDec(0, 0, inputReturnType, "input", inputParams, new NilExp(0, 0, null));
        NodeType inputNode = new NodeType("input", inputDec, 0);
        insert(currentScope, inputNode);

        SimpleDec outputParam = new SimpleDec(0, 0, new NameTy(0, 0, NameTy.INT), "someInt");
        VarDecList outputParams = new VarDecList(outputParam, null);
        NameTy outputReturnType = new NameTy(0, 0, NameTy.VOID);
        FunctionDec outputDec = new FunctionDec(0, 0, outputReturnType, "output", outputParams, new NilExp(0, 0, null));
        NodeType outputNode = new NodeType("output", outputDec, 0);
        insert(currentScope, outputNode);

        // maybe?
        inputDec.funAddr = 4;
        outputDec.funAddr = 7;

        // go through program and perform type checking
        while(list != null){
            if(list.head != null){
                typeCheck(list.head);
            }
            list = list.tail;
        }

        // global is done here
        removeScope();
    }

    // type checking methods for abstract classes to facilate which concretes should be used 
    private void typeCheck(Dec dec) {
        if (dec instanceof VarDec)
            typeCheck((VarDec)dec);
        else if (dec instanceof FunctionDec)
            typeCheck((FunctionDec)dec);
    } 

    private void typeCheck(VarDec varDec) {
        if (varDec instanceof SimpleDec) 
            typeCheck((SimpleDec)varDec);
        else if (varDec instanceof ArrayDec)
            typeCheck((ArrayDec)varDec);
    }

    private void typeCheck(Var var) {
        if (var instanceof SimpleVar) 
            typeCheck((SimpleVar)var); 
        else if (var instanceof IndexVar)
            typeCheck((IndexVar)var);
    }

    private void typeCheck(Exp exp) {
        if (exp instanceof AssignExp) 
            typeCheck((AssignExp)exp);
        else if (exp instanceof BoolExp) 
            typeCheck((BoolExp)exp);
        else if (exp instanceof CallExp) 
            typeCheck((CallExp)exp); 
        else if (exp instanceof CompoundExp) 
            typeCheck((CompoundExp)exp); 
        else if (exp instanceof IfExp) 
            typeCheck((IfExp)exp); 
        else if (exp instanceof IntExp) 
            typeCheck((IntExp)exp); 
        else if (exp instanceof NilExp) 
            typeCheck((NilExp)exp);
        else if (exp instanceof OpExp) 
            typeCheck((OpExp)exp);
        else if (exp instanceof ReturnExp) 
            typeCheck((ReturnExp)exp);
        else if (exp instanceof VarExp) 
            typeCheck((VarExp)exp);
        else if (exp instanceof WhileExp) 
            typeCheck((WhileExp)exp);
    }

    // type checking methods for concrete classes (children of Dec)
    private void typeCheck(FunctionDec fDec) {
        Exp body = fDec.body;

        // dealing with a function proto only 
        if (body.dtype == null) {
            typeCheckFunctionProto(fDec);

        }else { // otherwise we're dealing with an actual definition
            typeCheckFunctionDef(fDec);
        }
  
    }

    private void typeCheckFunctionProto(FunctionDec fDec) {
        NodeType funcNode = lookup(fDec.name, true);

        if (funcNode != null) {
            System.err.printf("ERROR: double declaration of function proto %s on line %d  column %d\n", fDec.name, fDec.row+1, fDec.col+1);
            error = true;
        }else {
            // need to add func proto to symbol table 
            insert(currentScope, new NodeType(fDec.name, fDec, currentScope));
        }
    }

    private void typeCheckFunctionDef(FunctionDec fDec) {
        String name = fDec.name;
        int returnType = fDec.result.type;
        VarDecList params = fDec.params;
        Exp body = fDec.body;
        FunctionDec fproto = null;

        NodeType functionNode = lookup(name, false);
        if(functionNode != null){
            fproto = (FunctionDec)functionNode.dec;
        }

        // dealing with a function proto only
        if (functionNode != null && fproto.body.dtype == null) {

            // check if function proto type matches the declaration type
            if(fproto.result.type != returnType){
                System.err.printf("ERROR: function prototype and function declaration having mismatching types: %s on line %d column %d expects type %s, received %s\n", fDec.name, fDec.row+1, fDec.col+1, typeNames.get(fproto.result.type), typeNames.get(returnType));
                error = true;
            }
            fproto.body = body;
            setReturnType(fproto.result.type);
            functionNames.add(name); // Add function name to list

            // add function name to current scope before creating new scope for function
            symbolTable.get(currentScope).remove(name);
            functionNode = new NodeType(name, fDec, currentScope);
            insert(currentScope, functionNode);

            int protoParams = countParams(fproto.params);
            int funcParams = countParams(fDec.params);

            if(protoParams != funcParams){
                System.err.printf("ERROR: incorrect number of paramters to function prototype: %s on line %d  column %d. Expected %d args, received %d args\n", fDec.name, fDec.row+1, fDec.col+1, protoParams, funcParams);
                error = true;
            }

            // loop through lists and verify that arguments match 
            while (params != null && fproto.params != null) {
                int curParamType = params.head.type.type; 
                DecInstance curParamDecInstance = getDecInstance(params.head);
                int curArgType = getType(fproto.params.head);
                DecInstance curArgDecInstance = getDecInstance(fproto.params.head);
                String argName = fproto.params.head.name;

                // need to perform look up on arg since a variable was used 
                if (curArgType == NameTy.UNKNOWN) {
                    NodeType var = lookup(argName, true);
                    if (var != null) {
                         curArgType = getType(var.dec);
                         curArgDecInstance = getDecInstance(var.dec);
                    }
                }

                if (curParamType != curArgType) {
                    System.err.printf("ERROR: mismatching argument types in function call on line %d  column %d. Expected %s received %s\n", fDec.row+1, fDec.col+1, typeNames.get(curParamType), typeNames.get(curArgType));
                    error = true;
                }else if (curParamDecInstance != curArgDecInstance) {
                    System.err.printf("ERROR: mismatching argument types in function call on line %d  column %d. Expected %s received %s\n", fDec.row+1, fDec.col+1, curParamDecInstance.name(), curArgDecInstance.name());
                    error = true;
                }

                params = params.tail;
                fproto.params = fproto.params.tail;
            }
            currentScope += 1;
            typeCheck(fDec.params);
            currentScope -= 1;
            typeCheck(fDec.body);

        } else { // otherwise we're dealing with an actual definition

            if (functionNode != null) {
                System.err.printf("ERROR: double declaration of function %s on line %d  column %d\n", name, fDec.row+1, fDec.col+1);
                error = true;
            } else {

                functionNames.add(name); // Add function name to list
                setReturnType(returnType); // Set function return type

                // add function name to current scope before creating new scope for function
                functionNode = new NodeType(name, fDec, currentScope);
                insert(currentScope, functionNode);

                currentScope += 1;
                typeCheck(fDec.params);
                currentScope -= 1;
                typeCheck(fDec.body);
            }
        }
    }

    private void typeCheck(SimpleDec sDec) {
        
        NodeType decNode = lookup(sDec.name, false); 

        if (decNode != null) {
            System.err.printf("ERROR: double declaration of variable %s on line %d  column %d\n", sDec.name, sDec.row+1, sDec.col+1);
            error = true;
            return;
        }

        if (isType(sDec, NameTy.VOID) && !sDec.name.equalsIgnoreCase("VOID(function arg)")) {
            System.err.printf("ERROR: variable %s of type VOID on line %d  column %d\n", sDec.name, sDec.row+1, sDec.col+1);
            error = true;
            return;
        }

        decNode = new NodeType(sDec.name, sDec, currentScope);
        insert(currentScope, decNode);

        if(currentScope > 0){
            sDec.nestLevel = 1;
        } else {
            sDec.nestLevel = 0;
        }
    }

    private void typeCheck(ArrayDec aDec) {
        
        // make sure size is valid 
        if (aDec.size < 0) {
            System.err.printf("ERROR: size of array %s must be >= 0 on line %d  column %d\n", aDec.name, aDec.row+1, aDec.col+1);
            error = true;
            return;
        }

        if (isType(aDec, NameTy.VOID)) {
            System.err.printf("ERROR: cannot use array %s of type VOID on line %s  column %d\n", aDec.name, aDec.row+1, aDec.col+1);
            error = true;
        }

        NodeType arrayNode = lookup(aDec.name, false);

        if (arrayNode != null) {
            System.err.printf("ERROR: double definition of array %s on line %s  column %s\n", aDec.name, aDec.row+1, aDec.col+1);
            error = true;
            return;
        }

        arrayNode = new NodeType(aDec.name, aDec, currentScope);
        insert(currentScope, arrayNode);

        if(currentScope > 0){
            aDec.nestLevel = 1;
        } else {
            aDec.nestLevel = 0;
        }
    }

    // type checking methods for concrete classes (children of Var)
    private void typeCheck(SimpleVar sVar) {

        NodeType symbol = lookup(sVar.name, true);

        if (symbol == null) {
            System.err.printf("ERROR: undefined variable: %s on line %d  column %d\n", sVar.name, sVar.row+1, sVar.col+1);
            error = true;
            return;
        }

        if (symbol.dec instanceof SimpleDec) {
            sVar.dec = (SimpleDec)symbol.dec;
            if (isType(symbol.dec, NameTy.VOID)) {
                System.err.printf("ERROR: cannot use variable %s of type VOID on line %d  column %d\n", sVar.name, sVar.row+1, sVar.col+1);
                error = true;
            }
        } else if (symbol.dec instanceof ArrayDec)
            sVar.dec = (ArrayDec)symbol.dec;
        
    }

    private void typeCheck(IndexVar iVar) {
        
        NodeType symbol = lookup(iVar.name, true);
        int indexType = getType(iVar.index.dtype);

        if (indexType == NameTy.UNKNOWN) {
            NodeType node = lookup(iVar.index.dtype.name, true);
            if (node != null) {
                indexType = getType(node.dec);
            }
            //System.err.println("Index VAR: " + iVar.index.dtype.name + " nest level = " + ((SimpleDec)node.dec).nestLevel);
        }

        if ((indexType != NameTy.INT)) {
            System.err.printf("ERROR: invalid index expression for %s on line %d  column %d\n", iVar.name, iVar.row+1, iVar.col+1);
            error = true;
            return;
        }

        if (symbol == null) {
            System.err.printf("ERROR: undefined variable: %s on line %d  column %d\n", iVar.name, iVar.row+1, iVar.col+1);
            error = true;
        }

        if (symbol.dec instanceof ArrayDec && iVar.index instanceof IntExp) {
            ArrayDec arr = (ArrayDec)symbol.dec;
            IntExp varIntExp = (IntExp)iVar.index;
            int varInt = Integer.parseInt(varIntExp.value);

            if(!(varInt >= 0 && varInt < arr.size)){
                error = true;
                System.err.printf("ERROR: Index out of bounds for %s on line %d  column %d\n", iVar.name, iVar.index.row+1, iVar.index.col+1);
            }

            if (isType(symbol.dec, NameTy.VOID)) {
                error = true;
                System.err.printf("ERROR: cannot use variable %s of type VOID on line %d  column %d\n", iVar.name, iVar.row+1, iVar.col+1);
            }

            iVar.dec = arr;
        } else if(symbol.dec instanceof ArrayDec && iVar.index instanceof OpExp){
            OpExp op = (OpExp)iVar.index;
            if(op.left.dtype == null){
                error = true;
                System.err.printf("ERROR: Index out of bounds for %s on line %d  column %d\n", iVar.name, iVar.index.row+1, iVar.index.col+1);
            }
        }

        if (symbol.dec instanceof ArrayDec) {
            iVar.dec = (ArrayDec)symbol.dec;
        } 

        typeCheck(iVar.index);

    }
   
    // type checking methods for concrete classes (children of Exp)
    private void typeCheck(AssignExp aExp) {
        
        String leftSideVarName = aExp.lhs.variable.name;
        NodeType leftSideVar = lookup(leftSideVarName, true);

        if (leftSideVar == null) {
            error = true;
            System.err.printf("ERROR: attempted assignment to undefined variable %s on line %d  column %d\n", leftSideVarName, aExp.row+1, aExp.lhs.col+1);
            return;
        }

        int leftType = getType(leftSideVar.dec); 
        int rightType = getType(aExp.rhs.dtype);
        DecInstance leftInstance = getDecInstance(leftSideVar.dec);
        DecInstance rightInstance = getDecInstance(aExp.rhs.dtype);

        // If the type is unknown for right exp, then look up in table and change the type + Dec instance
        if (rightType == NameTy.UNKNOWN) {
            NodeType node = lookup(aExp.rhs.dtype.name, true);
            if (node != null){
                rightType = getType(node.dec);
                rightInstance = getDecInstance(node.dec);
            }
        }

        // Check if left hand expression is referencing an array index
        if (aExp.lhs.variable instanceof IndexVar){
            leftInstance = DecInstance.SIMPLE;
        }

        // Check if right hand expression is referencing an array index
        if (aExp.rhs instanceof VarExp){
            VarExp var = (VarExp)aExp.rhs;
            if(var.variable instanceof IndexVar)
                rightInstance = DecInstance.SIMPLE;
        }

        // check if the right instance is function, and change it to simple since we only care for the type
        if(rightInstance == DecInstance.FUNCTION){
            rightInstance = DecInstance.SIMPLE;
        }

        // mismatching types (INT/VOID/BOOL)
        if (leftType != rightType) { 
            error = true;
            System.err.printf("ERROR: invalid assignment on line %d  column %d. Cannot assign %s to %s\n", aExp.row+1, aExp.col+1, typeNames.get(leftType), typeNames.get(rightType));
            return;
        }

        // mismatching dec instances (eg. trying to assign myArray (arrayDec) = 4(simpleDec))
        if (leftInstance != rightInstance) {
            error = true;
            System.err.printf("ERROR: invalid assignment on line %d  column %d. Cannot assign %s to %s\n", aExp.row+1, aExp.col+1, leftInstance.name(), rightInstance.name());
        }
     
        typeCheck(aExp.lhs);
        typeCheck(aExp.rhs);
    }

    private void typeCheck(BoolExp bExp) {
    
    }

    private void typeCheck(CallExp cExp) {

        NodeType funcToCall = lookup(cExp.func, true);
        ExpList callingArgs = cExp.args;
        VarDecList funcParams;

        if (funcToCall == null) {
            error = true;
            System.err.printf("ERROR: attempted call to undefined function: %s on line %d  column %d\n", cExp.func, cExp.row+1, cExp.col+1);

        } else {
            cExp.dtype = (FunctionDec)funcToCall.dec;
            typeCheck(callingArgs);
            
            funcParams = ((FunctionDec)funcToCall.dec).params;
            int callingArgsSize = countArgs(callingArgs);
            int funcParamsSize = countParams(funcParams);

            if (callingArgsSize != funcParamsSize) {
                error = true;
                System.err.printf("ERROR: incorrect number of arguments in call to function: %s on line %d  column %d. Expected %d args, received %d args\n", cExp.func, cExp.row+1, cExp.col+1, funcParamsSize, callingArgsSize);
            }

            // loop through lists and verify that arguments match 
            while (funcParams != null && callingArgs != null) {
                int curParamType = funcParams.head.type.type; 
                DecInstance curParamDecInstance = getDecInstance(funcParams.head);
                int curArgType = getType(callingArgs.head.dtype);
                DecInstance curArgDecInstance = getDecInstance(callingArgs.head.dtype);
                String argName = callingArgs.head.dtype.name;

                // need to perform look up on arg since a variable was used 
                if (curArgType == NameTy.UNKNOWN) {
                    NodeType var = lookup(argName, true);
                    if (var != null) {
                         curArgType = getType(var.dec);
                         curArgDecInstance = getDecInstance(var.dec);
                    }
                }

                if(cExp.args.head instanceof VarExp){
                    VarExp var = (VarExp)cExp.args.head;
                    if (var.variable instanceof IndexVar){
                        curArgDecInstance = DecInstance.SIMPLE;
                    }
                }

                if (curParamType != curArgType) {
                    error = true;
                    System.err.printf("ERROR: mismatching argument types in function call on line %d  column %d. Expected %s received %s\n", cExp.row+1, cExp.col+1, typeNames.get(curParamType), typeNames.get(curArgType));
                }

                if(curParamDecInstance != curArgDecInstance && curArgDecInstance != DecInstance.FUNCTION){
                    error = true;
                    System.err.printf("ERROR: mismatching argument types in function call on line %d  column %d. Expected %s received %s\n", cExp.row+1, cExp.col+1, curParamDecInstance.name(), curArgDecInstance.name());
                }

                funcParams = funcParams.tail;
                callingArgs = callingArgs.tail;
            }
        }
    }

    private void typeCheck(CompoundExp cmpExp) {
        functionNames.add(""); // add empty string to signify a block
        setNewScope();
        typeCheck(cmpExp.decs);
        typeCheck(cmpExp.exps);
        removeScope();
    }

    private void typeCheck(IfExp ifExp) {
        typeCheck(ifExp.test);
        typeCheck(ifExp.thenpart);

        if (ifExp.elsepart.dtype != null) {
            typeCheck(ifExp.elsepart);
        }
    }

    private void typeCheck(IntExp intExp) {
        
    }

    private void typeCheck(NilExp nExp) {
        // do nothing        
    }

    private void typeCheck(OpExp oExp) {

        int opType = determineOperatorCategory(oExp.op);
        int rightType = getType(oExp.right.dtype);
        int leftType = getType(oExp.left.dtype);
        DecInstance leftInstance = getDecInstance(oExp.right.dtype);
        DecInstance rightInstance = getDecInstance(oExp.left.dtype);

        if (rightType == NameTy.UNKNOWN){
            NodeType node = lookup(oExp.right.dtype.name, true);
            if (node != null)
                rightType = getType(node.dec);
        }

        if (leftType == NameTy.UNKNOWN){
            NodeType node = lookup(oExp.left.dtype.name, true);
            if (node != null)
                leftType = getType(node.dec);
        }
      
        // trying to perform operation on functions
        if (leftInstance == DecInstance.FUNCTION || rightInstance == DecInstance.FUNCTION) {
            error = true;
            System.err.printf("ERROR: cannot perform operations with functions on line %d  column %d\n", oExp.row+1, oExp.col+1);
        }

        // comparison operator: left and right side need to be the same type  
        // 0 = comparison operator  1 = arithmetic operator  2 = logical operator  3 = UMINUS
        if (opType == 0 && rightType != leftType) {
            error = true;
            System.err.printf("ERROR: invalid operation on line %d  column %d. Cannot perform comparison on %s to %s\n", oExp.row+1, oExp.col+1, typeNames.get(leftType), typeNames.get(rightType));
        }

        // arithmetic operator, both sides must be of type int and cannot divide by 0
        if (opType == 1) {

            if (leftType != NameTy.INT || rightType != NameTy.INT) {
                error = true;
                System.err.printf("ERROR: cannot perform arithmetic operation on non integer types on line %d  column %d\n", oExp.row+1, oExp.col+1);
            } else if (oExp.op == OpExp.DIV && !(oExp.right instanceof VarExp)) {
                int divValue = Integer.parseInt(((IntExp)oExp.right).value);
                if (divValue == 0) {
                    error = true;
                    System.err.printf("ERROR: attempted division by zero error on line %d  column %d\n", oExp.row+1, oExp.col+1);
                }
            }
        }

        // logical operator, both sides must be of type bool 
        if (opType == 2) {
            if (leftType != NameTy.BOOL || rightType != NameTy.BOOL){
                error = true;
                System.err.printf("ERROR: cannot perform logical operation on non boolean types on line %d  column %d\n", oExp.row+1, oExp.col+1);
            }
        }

        // UMINUS, right side must be of type BOOL 
        if (opType == 3) {
            if (rightType != NameTy.BOOL) {
                error = true;
                System.err.printf("ERROR: cannot perform negation (uminus) operation on non boolean type on line %d  column %d\n", oExp.row+1, oExp.col+1);
            }
        }
        
        typeCheck(oExp.left);
        typeCheck(oExp.right);
    }

    private void typeCheck(ReturnExp rExp) {

        if (currentReturnType == NameTy.VOID && rExp.exp.dtype != null) {   
            error = true;         
            System.err.printf("ERROR: attempting to return from a VOID function on line %d  column %d\n", rExp.row+1, rExp.col+1);
        }else if (currentReturnType != NameTy.VOID && rExp.exp.dtype == null) {
            error = true;
            System.err.printf("ERROR: non VOID function has no return value on line %d  column %d\n", rExp.row+1, rExp.col+1);
        }else {
            int rType = getType(rExp.dtype);

            // unknown type, need to look it up
            if (rType == NameTy.UNKNOWN && rExp.exp.dtype != null) {
                NodeType node = lookup(rExp.exp.dtype.name, printSymbolTable);
                if (node != null)
                    rType = getType(node.dec);
            }

            if(rType == NameTy.UNKNOWN && rExp.exp.dtype == null){
                rType = NameTy.VOID;
            }

            if (rType != currentReturnType) {
                error = true;
                System.err.printf("ERROR: mismatching return type. Expected type: %s  found type: %s on line %d  column %d\n", typeNames.get(currentReturnType), typeNames.get(rType), rExp.row+1, rExp.col+1);
            }
        }

        typeCheck(rExp.exp);
    }

    private void typeCheck(VarExp vExp) {
        typeCheck(vExp.variable);
    }

    private void typeCheck(WhileExp wExp) {
        typeCheck(wExp.test);
        typeCheck(wExp.body);
    }

    // type checking methods for concrete classes (misc. classes)
    private void typeCheck(VarDecList vList) {
        
        // type check all vars 
        while(vList != null){
            if(vList.head != null){
                typeCheck(vList.head);
            }
            vList = vList.tail;
        }
    }

    private void typeCheck(ExpList eList) {
       
        // type check all expressions 
        while(eList != null){
            if(eList.head != null){
                typeCheck(eList.head);
            }
            eList = eList.tail;
        }
    }

    private int getType(Dec dtype) {

        NameTy decType;

        if(dtype == null){
            return NameTy.UNKNOWN;
        }

        if (dtype instanceof FunctionDec) {
            FunctionDec fd = (FunctionDec)dtype;
            decType = fd.result;
        } else if (dtype instanceof SimpleDec) {
            SimpleDec sd = (SimpleDec)dtype;
            decType = sd.type;
        } else {
            ArrayDec ad = (ArrayDec)dtype;
            decType = ad.type;
        }

        return decType.type;
    }

    private DecInstance getDecInstance(Dec dec) {
        if (dec == null) 
            return DecInstance.UNEXPECTED;
        else if (dec instanceof FunctionDec) 
            return DecInstance.FUNCTION;
        else if (dec instanceof ArrayDec) 
            return DecInstance.ARRAY;
        else if (dec instanceof SimpleDec) 
            return DecInstance.SIMPLE;
        
        return DecInstance.UNEXPECTED;
    }

    private boolean isType(Dec dtype, int type) {
        int varType = getType(dtype);
        return varType == type;
    }

    private void setNewScope() {
        String funcName = "";
        // checks the second last index to see if it is a function name, if not, then we know it is a block
        if (functionNames.size() - 1 > 0){
            funcName = functionNames.get(functionNames.size() - 2);
        }
        visitor.newScope(symbolTable, currentScope + 1, funcName);
        currentScope++;
    }

    private void removeScope() {
        String funcName = "";
        // checks the second last index to see if it is a function name, if not, then we know it is a block, and finally remove the last index;
        if (functionNames.size() - 1 > 0){
            funcName = functionNames.get(functionNames.size() - 2);
            functionNames.remove(functionNames.size() - 1);
        }
        visitor.removeScope(symbolTable, currentScope, funcName);
        delete(currentScope);
        currentScope--;
    }

    private void setReturnType(int type) {
        currentReturnType = type;
    }

    public void insert(int scopeKey, NodeType node) {
        HashMap<String, NodeType> nodeList = symbolTable.get(scopeKey);

        if (nodeList == null){
            nodeList = new HashMap<String, NodeType> ();
            nodeList.put(node.name, node);
            symbolTable.put(scopeKey, nodeList);
        }
        else{
            if(!nodeList.containsKey(node.name)){
                nodeList.put(node.name, node);
            }
        }
    }

    // try to find a node from the symbol table
    public NodeType lookup(String name, boolean checkAllScopes) {
        
        // if we are doing a declaration, we only want to check the current scope since multiple decs across scopes are allowed
        if (!checkAllScopes) {
            HashMap<String, NodeType> currentScopeVariables = symbolTable.get(currentScope);

            if (currentScopeVariables == null) return null;
            
            return currentScopeVariables.get(name);
        }

        // otherwise, we are referencing the variable meaning we need to check all scopes (we assume the first one we find is the one we want)
        int scopeIndex = currentScope;
        NodeType foundNode = null;

        while (scopeIndex != -1) {
            HashMap<String, NodeType> currentScopeVariables = symbolTable.get(scopeIndex);

            if(currentScopeVariables == null){
                scopeIndex--;
                continue;
            }

            foundNode = currentScopeVariables.get(name);

            if (foundNode != null) 
                return foundNode;

            scopeIndex--;
        }

        return foundNode;
    }
    public void delete (int scope){
        symbolTable.remove(scope);
    }

    /* 
        given an op, return what "category" it belongs to
        0 = comparison operator  1 = arithmetic operator  2 = logical operator  3 = UMINUS
    */
    private int determineOperatorCategory(int operator) {

        if (COMPARISON_OPS.contains(operator))
            return 0;
        else if (ARITHMETIC_OPS.contains(operator))
            return 1;
        else if (LOGICAL_OPS.contains(operator))
            return 2;
        else 
            return 3;
    }

    private int countParams(VarDecList params) {
        if(params != null){
            if (params.head.type.type == NameTy.VOID)  return 0;
        }   
        
        int count = 0;
        while (params != null) {
            count++;
            params = params.tail;
        }
        return count;
    }
    
    private int countArgs(ExpList args) {
        int count = 0;
        while (args != null) {
            count++;
            args = args.tail;
        }
        return count;
    }

    private void initTypeNamesMap() {
        typeNames = new HashMap<Integer, String>();
        typeNames.put(0, "BOOL");
        typeNames.put(1, "INT");
        typeNames.put(2, "VOID");
        typeNames.put(3, "MISSING");
        typeNames.put(4, "UNKNOWN");
    }

}