package c1Absyn;

public class DecList extends Absyn {
    public Dec head;
    public DecList tail; 

    public DecList(Dec head, DecList tail) {
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr) {
        visitor.visit(this, level, isAddr);
    }
}
