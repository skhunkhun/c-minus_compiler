package c1Absyn;

public class CompoundExp extends Exp{

    public VarDecList decs;
    public ExpList exps;

    public CompoundExp( int row, int col, VarDecList decs, ExpList exps, Dec dtype ) {
        super(dtype);
        this.row = row;
        this.col = col;
        this.decs = decs;
        this.exps = exps;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr ) {
        visitor.visit( this, level, isAddr );
    }
    
}
