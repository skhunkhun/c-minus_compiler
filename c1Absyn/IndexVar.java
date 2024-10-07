package c1Absyn;

public class IndexVar extends Var{
    public Exp index; 
    public ArrayDec dec;

    public IndexVar(int row, int col, String name, Exp index) {
        super(row, col, name);
        this.index = index;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
