package c1Absyn;

public class AssignExp extends Exp {
  public VarExp lhs;
  public Exp rhs;

  public AssignExp( int row, int col, VarExp lhs, Exp rhs, Dec dtype) {
    super(dtype);
    this.row = row;
    this.col = col;
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }
}
