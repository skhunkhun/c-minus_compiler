package c1Absyn;

public class OpExp extends Exp {
  public final static int PLUS        = 0;
  public final static int MINUS       = 1;
  public final static int UMINUS      = 2;
  public final static int TIMES       = 3;
  public final static int DIV         = 4;
  public final static int EQ          = 5;
  public final static int NEQ         = 6;
  public final static int LT          = 7;
  public final static int LTE         = 8;
  public final static int GT          = 9;
  public final static int GTE         = 10;
  public final static int AND         = 11;
  public final static int OR          = 12;
  public final static int NOT         = 13;

  public Exp left;
  public int op;
  public Exp right;

  public OpExp( int row, int col, Exp left, int op, Exp right, Dec dtype ) {
    super(dtype);
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
    visitor.visit( this, level, isAddr );
  }
}
