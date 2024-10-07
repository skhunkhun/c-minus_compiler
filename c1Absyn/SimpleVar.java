package c1Absyn;

public class SimpleVar extends Var {

    public SimpleVar(int row, int col, String name) {
        super(row, col, name);
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
