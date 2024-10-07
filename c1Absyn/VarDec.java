package c1Absyn;
public abstract class VarDec extends Dec{
    public NameTy type; 
    public int offset;
    public int nestLevel;

    public VarDec(int row, int col, NameTy type, String name) {
        super(name);
        this.row = row;
        this.col = col; 
        this.type = type; 
    }
}
