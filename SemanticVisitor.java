import java.util.HashMap;
import java.util.Map;

import c1Absyn.*;

public class SemanticVisitor {

    final static int SPACES = 4;

    // Calculates and returns a string carrying the proper indent
    private String indent( int level ) {
        String indStr = "";
        for(int i = 0; i < level * SPACES; i++){
            indStr += (" ");
        }

        return indStr;
    }

    // Print the current scope
    public void printScope(HashMap<Integer, HashMap<String, NodeType>> symbolTable, int currentScope){
        HashMap<String, NodeType> currentScopeVariables = symbolTable.get(currentScope);
        if(currentScopeVariables == null){
            return;
        }
        for (Map.Entry<String, NodeType> entry : currentScopeVariables.entrySet()) {
            String name = entry.getKey();
            NodeType type = entry.getValue();
            
            if (type.dec instanceof SimpleDec){
                SimpleDec sd = (SimpleDec)type.dec;
                System.out.println(indent(currentScope + 1) + "VarDec - " + name + ": " + printType(sd.type.type));

            } else if(type.dec instanceof ArrayDec){
                ArrayDec ad = (ArrayDec)type.dec;
                System.out.println(indent(currentScope + 1) + "ArrayDec - " + name + ": " + printType(ad.type.type));
            } else if(type.dec instanceof FunctionDec){
                FunctionDec fd = (FunctionDec)type.dec;
                System.out.println(indent(currentScope + 1) + "FuncDec - " + name + ": " + printType(fd.result.type));
            }else {
                System.out.println(indent(currentScope + 1) + name + ": " + type);
            }
            
        }
    }

    // New scope
    public void newScope(HashMap<Integer, HashMap<String, NodeType>> symbolTable, int currentScope, String functionName){

        if (currentScope == 0){
            System.out.println(indent(currentScope) + "Entering Global Scope:");
            return;
        }

        if (functionName != ""){
            System.out.println(indent(currentScope) + "Entering scope for function: " + functionName);
        } else {
            System.out.println(indent(currentScope) + "Entering the new block:");
        }
    }

    // Print table before removing scope
    public void removeScope(HashMap<Integer, HashMap<String, NodeType>> symbolTable, int currentScope, String functionName){
        if (currentScope == 0){
            printScope(symbolTable, currentScope);
            System.out.println(indent(currentScope) + "Leaving Global Scope:");
            return;
        }

        if (functionName != ""){
            printScope(symbolTable, currentScope);
            System.out.println(indent(currentScope) + "Leaving scope for function: " + functionName);
        } else {
            printScope(symbolTable, currentScope);
            System.out.println(indent(currentScope) + "Leaving the block:");
        }
    }

    public String printType(int type){
        String result = "";
        if(type == NameTy.BOOL){
            result = "BOOL";
        } else if(type == NameTy.INT){
            result = "INT";
        } else if(type == NameTy.VOID){
            result = "VOID";
        } else {
            result = "UNKNOWN";
        }
        return result;
    }
}