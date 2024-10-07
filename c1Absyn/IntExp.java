package c1Absyn;

public class IntExp extends Exp {
  public String value;

  public IntExp( int row, int col, String value, Dec dtype ) {
    super(dtype);
    this.row = row;
    this.col = col;
    this.value = value;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }
}
