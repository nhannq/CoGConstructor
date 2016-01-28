package testers;

import java.util.Iterator;

public class CallGraphs {

  Cell[] cells;

  class Cell {
    private int cell = 0;

    public Cell(int cell) {
      this.cell = cell;
    }

    public int getCell() {
      return cell;
    }
  }

  class CellIterator implements Iterator<Cell> {
    private int idx, end;

    public CellIterator(int idx, int end) {
      this.idx = idx;
      this.end = end;
    }

    public boolean hasNext() {
      return idx < end;
    }

    public Cell next() {
      return cells[idx++];
    }

    public void remove() {
    }
  }

  public static void main(String[] args) {
    CallGraphs cG = new CallGraphs();
    cG.testIterator();
  }

  void testIterator() {
    cells = new Cell[5];
    Cell c0 = new Cell(0);
    Cell c1 = new Cell(1);
    Cell c2 = new Cell(2);
    cells[0] = c0;
    cells[1] = c1;
    cells[2] = c2;
    Iterator<Cell> iterator = new CellIterator(0, 3);
    while (iterator.hasNext()) {
      Cell c = iterator.next();
      System.out.println(c.getCell());
    }
  }
}