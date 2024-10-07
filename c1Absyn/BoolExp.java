package c1Absyn;

public class BoolExp extends Exp{
    public String value;

    public BoolExp( int row, int col, String value, Dec dtype) {
        super(dtype);
        this.row = row;
        this.col = col;
        this.value = value;
    }
    
    public void accept(AbsynVisitor visitor, int level, boolean isAddr){
        visitor.visit(this, level, isAddr);

    }
}
