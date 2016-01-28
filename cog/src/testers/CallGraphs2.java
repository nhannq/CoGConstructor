package testers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CallGraphs2 {

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

  // public static void main(String[] args) {
  // // doStuff();
  // CallGraphs cG = new CallGraphs();
  // // cG.doStuff2(3);
  //
  // int a = 0;
  //
  // if (a > 10) {
  // cG.p1(a);
  // String s = "";
  // if (!s.equals("")) {
  // System.out.println(s);
  // }
  // } else if (a < 0) {
  // cG.p2(a);
  // } else if (1 < a && a < 5) {
  // cG.p4(a);
  // } else {
  // cG.p3(a);
  // }
  //
  // System.out.println("CHECK");
  //
  // // Cell c1 = new Cell(0);
  //
  //
  //
  // }

  public static void main(String[] args) {
    CallGraphs2 cG = new CallGraphs2();
    cG.testIterator();
    // cG.runIterator();
  }

  // void runIterator() {
  // List<String> stringList = new ArrayList<String>() {
  // {
  // add("a");
  // add("b");
  // add("c");
  // }
  // };
  // Iterator<String> iterator = stringList.iterator();
  // while (iterator.hasNext()) {
  // System.out.println(iterator.next());
  // }
  // }

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

  // void p1(int a) {
  // Cell c1 = new Cell(0);
  // }
  //
  // void p2(int a) {
  //
  // }
  //
  // void p3(int a) {
  //
  // }
  //
  // void p4(int a) {
  // p3(a);
  // }
  //
  // public void doStuff2(int a) {
  // doStuff();
  // List<Integer> b = new ArrayList<Integer>();
  // b.add(doStuff());
  // }
  //
  // public static int doStuff() {
  // // A a = new A();
  // // a.foo();
  // // a.bar();
  // return 1;
  // }
}


// class A {
// public void foo() {
// bar();
// }
//
// public void bar() {}
// }
