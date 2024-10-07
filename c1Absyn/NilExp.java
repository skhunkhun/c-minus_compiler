package c1Absyn;

public class NilExp extends Exp{

    public NilExp( int row, int col, Dec dtype) {
        super(dtype);
        this.row = row;
        this.col = col;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
        visitor.visit( this, level, isAddr );
    }
    
}
