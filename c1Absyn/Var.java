package c1Absyn;

public abstract class Var extends Absyn { 
    public String name; 
    public VarDec dec;

    public Var(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }
}
