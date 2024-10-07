package c1Absyn;

public class FunctionDec extends Dec{
    public NameTy result;
    // public String func;
    public VarDecList params; 
    public Exp body; 
    public int funAddr;

    public FunctionDec(int row, int col, NameTy result, String func, VarDecList params, Exp body) {
        super(func);
        this.row = row;
        this.col = col;
        this.result = result; 
        this.params = params; 
        this.body = body;
        this.funAddr = -1;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
