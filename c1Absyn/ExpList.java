package c1Absyn;

public class ExpList extends Absyn {
    public Exp head; 
    public ExpList tail; 
    
    public ExpList(Exp head, ExpList tail) {
        this.head  = head; 
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}