package c1Absyn;

public class ReturnExp extends Exp{

    public Exp exp;

    public ReturnExp( int row, int col, Exp exp, Dec dtype ) {
        super(dtype);
        this.row = row;
        this.col = col;
        this.exp = exp;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
        visitor.visit( this, level, isAddr );
    }
    
}
