package c1Absyn;

abstract public class Absyn {
  public int row, col;

  abstract public void accept( AbsynVisitor visitor, int level, boolean isAddr );
}
