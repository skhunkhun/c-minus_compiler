package c1Absyn;

abstract public class Exp extends Absyn {
    public Dec dtype; //todo: need to add this boy to the constructors of all expression classes 

    public Exp(Dec dtype){
        this.dtype = dtype;
    }
}
