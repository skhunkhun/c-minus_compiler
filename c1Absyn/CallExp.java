package c1Absyn;

public class CallExp extends Exp {
    public String func;
    public ExpList args;

    public CallExp( int row, int col, String func, ExpList args, Dec dtype ) {
        super(dtype);
        this.row = row;
        this.col = col;
        this.func = func;
        this.args = args;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
        visitor.visit( this, level, isAddr );
    }
    
}
