package c1Absyn;

public class ArrayDec extends VarDec {
    public int size; 

    public ArrayDec(ArrayDec dec) {
        super(dec.row, dec.col, dec.type, dec.name);
        this.size = dec.size;
    }

    public ArrayDec(int row, int col, NameTy type, String name, int size) {
        super(row, col, type, name);
        this.size = size; 
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
