package c1Absyn;

public class WhileExp extends Exp {

    public Exp test;
    public Exp body;

    public WhileExp( int row, int col, Exp test, Exp body, Dec dtype ) {
        super(dtype);
        this.row = row;
        this.col = col;
        this.test = test;
        this.body = body;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
        visitor.visit( this, level, isAddr );
    }
    
}
