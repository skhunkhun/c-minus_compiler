/* 
  This file is used to define all of the visit methods for our tree visitor
 */

import c1Absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( DecList decList, int level, boolean isAddr ){
    while( decList != null ) {
      if(decList.head != null) {
        decList.head.accept( this, level, isAddr );
      }
      decList = decList.tail;
    } 
  }

  public void visit( VarDecList varList, int level, boolean isAddr ){
    while( varList != null ) {
      // System.out.println(varList.head.name);
      if(varList.head != null){
        varList.head.accept( this, level, isAddr );
      }
      varList = varList.tail;
    } 
  }

  public void visit( ExpList expList, int level, boolean isAddr ){
    while( expList != null ) {
      if(expList.head != null)
        expList.head.accept( this, level, isAddr );
      expList = expList.tail;
    } 
  }

  public void visit( CompoundExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println("CompoundExp: ");
    
    if (exp.decs != null && exp.exps != null)
      level++;

    if (exp.decs != null)
      exp.decs.accept(this, level, isAddr);
    if (exp.exps != null)
      exp.exps.accept(this, level, isAddr);
  }

  public void visit( ReturnExp exp, int level, boolean isAddr ){ // maybe?
    indent( level );
    System.out.println( "ReturnExp:" );
    level++;
    exp.exp.accept( this, level, isAddr);
  }

  public void visit( WhileExp exp, int level, boolean isAddr ){ // maybe?
    indent( level );
    System.out.println( "WhileExp:" );
    level++;
    exp.test.accept( this, level, isAddr );
    exp.body.accept( this, level, isAddr) ;
  }

  public void visit( IfExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level, isAddr );
    exp.thenpart.accept( this, level, isAddr );
    if (exp.elsepart != null )
      exp.elsepart.accept( this, level, isAddr );

  }

  public void visit( AssignExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "AssignExp: = " );
    level++;
    exp.lhs.accept( this, level, isAddr );
    exp.rhs.accept( this, level, isAddr );
  }

  public void visit( OpExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.UMINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.DIV:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " == " );
        break;
      case OpExp.NEQ:
        System.out.println( " != " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.LTE:
        System.out.println( " <= " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.GTE:
        System.out.println( " >= " );
        break;
      case OpExp.AND:
        System.out.println( " && " );
        break;
      case OpExp.OR:
        System.out.println( " || " );
        break;
      case OpExp.NOT:
        System.out.println( " ~ " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
      exp.left.accept( this, level, isAddr );
    if (exp.right != null)
      exp.right.accept( this, level, isAddr );
  }

  public void visit( CallExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println("CallExp: " + exp.func);
    level++;
    if (exp.args != null)
      exp.args.accept(this, level, isAddr);
  }

  public void visit( VarExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "VarExp: ");
    level++;
    if (exp.variable!=null)
      exp.variable.accept(this,level, isAddr);
  }

  public void visit( BoolExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "BoolExp: " + exp.value);
  }

  public void visit( IntExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( NilExp exp, int level, boolean isAddr ){
    indent( level );
    System.out.println( "NilExp: "); 
  }

  public void visit( SimpleVar var, int level, boolean isAddr ){
    indent( level );
    System.out.println( "SimpleVar: " + var.name ); 

  }

  public void visit( IndexVar var, int level, boolean isAddr ){
    indent( level );
    System.out.println( "IndexVar:" );
    level++;
    var.index.accept( this, level, isAddr);
  }

  public void visit( FunctionDec dec, int level, boolean isAddr ){
    indent( level );
    if (dec.result.type == NameTy.BOOL){
      System.out.println("FunctionDec: " + dec.name + " - BOOL");
    } else if (dec.result.type == NameTy.INT){
      System.out.println("FunctionDec: " + dec.name + " - INT");
    } else if (dec.result.type == NameTy.VOID){
      System.out.println("FunctionDec: " + dec.name + " - VOID"); 
    }

    level++;

    if (dec.params != null)
      dec.params.accept(this, level, isAddr);
  
    if (dec.body != null)
      dec.body.accept(this, level, isAddr);

  }

  public void visit( SimpleDec dec, int level, boolean isAddr ){
    indent( level );
    if (dec.type.type == NameTy.BOOL){
      System.out.println("SimpleDec: " + dec.name + " - BOOL");
    } else if (dec.type.type == NameTy.INT){
      System.out.println("SimpleDec: " + dec.name + " - INT");
    } else if (dec.type.type == NameTy.VOID){
      System.out.println("SimpleDec: " + dec.name + " - VOID"); 
    }
  }

  public void visit( ArrayDec dec, int level, boolean isAddr ){
    indent(level);
    String type = "";

    if (dec.type.type == NameTy.BOOL) {
      type = "BOOL";
    } else if (dec.type.type == NameTy.INT) {
        type = "INT";
    } else if (dec.type.type == NameTy.VOID) {
      type = "VOID";
    }
    
    if (dec.size != 0) {
        System.out.println("ArrayDec: " + dec.name + "[" + dec.size + "]" + " - " + type);
    } else {
        System.out.println("ArrayDec: " + dec.name + "[]" + " - " + type);
    }

  }

  public void visit( NameTy type, int level, boolean isAddr ){
    indent( level );
    System.out.print( "NameTy:" ); 
    switch( type.type ) {
      case NameTy.BOOL:
        System.out.println( " bool " );
        break;
      case NameTy.INT:
        System.out.println( " int " );
        break;
      case NameTy.VOID:
        System.out.println( " void " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + type.row + " and column " + type.col);
    }
    level++;
    type.accept( this, level, isAddr );
  }
}