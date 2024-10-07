package c1Absyn;

public class SimpleDec extends VarDec{

    public SimpleDec(SimpleDec dec) {
        super(dec.row, dec.col, dec.type, dec.name);
    }

    public SimpleDec(int row, int col, NameTy type, String name) {
        super(row, col, type, name);
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
