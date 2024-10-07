package c1Absyn;

public interface AbsynVisitor {

  public void visit( DecList dec, int level, boolean isAddr );

  public void visit( VarDecList var, int level, boolean isAddr );

  public void visit( ExpList exp, int level, boolean isAddr );

  public void visit( CompoundExp exp, int level, boolean isAddr );

  public void visit( ReturnExp exp, int level, boolean isAddr );

  public void visit( WhileExp exp, int level, boolean isAddr );

  public void visit( IfExp exp, int level, boolean isAddr );

  public void visit( AssignExp exp, int level, boolean isAddr );

  public void visit( OpExp exp, int level, boolean isAddr );

  public void visit( CallExp exp, int level, boolean isAddr );

  public void visit( VarExp exp, int level, boolean isAddr );

  public void visit( BoolExp exp, int level, boolean isAddr );

  public void visit( IntExp exp, int level, boolean isAddr );

  public void visit( NilExp exp, int level, boolean isAddr );

  public void visit( SimpleVar var, int level, boolean isAddr );

  public void visit( IndexVar var, int level, boolean isAddr );

  public void visit( FunctionDec dec, int level, boolean isAddr );

  public void visit( SimpleDec dec, int level, boolean isAddr );

  public void visit( ArrayDec dec, int level, boolean isAddr );

  public void visit( NameTy type, int level, boolean isAddr );

}
