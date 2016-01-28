package edu.uconn.cse.cog.constructor.ccg;

import edu.uconn.cse.cog.model.CCGNode;
import edu.uconn.cse.cog.model.CCGraph;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class CopyOfCallGraphBuilderAbstract {

  public static int countMatch;

  public CCGraph graph;// = new CCGraph();

  protected static String programPrefix;

  public static void readFile(String fileName, List<String> data) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      try {
        String line = br.readLine();
        while (line != null) {
          data.add(line.trim());
          // System.out.println("LINE " + line);
          line = br.readLine();
        }
      } finally {
        br.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void printInvokeStmtLineNumber(String callsiteInfo, InvokeExpr vI,
      String methodName, int lineNumber) {
    // System.out.println("line number with virtualinvoke: " + vI.getMethodRef().name() + " at "
    // + lineNumber);

    // TODO: commented to test
    if (vI.getMethodRef().name().equals(methodName)) {
      // System.out.print(callsiteInfo + " " + vI.getMethodRef().name() + ": ");
      // for (int j = 0; j < vI.getArgCount(); j++) {
      // System.out.print(vI.getArg(j) + ", ");
      // }
      // System.out.println(" at " + lineNumber);
      // System.out.println();
      // }

      // added to test
      System.out.println(lineNumber);
    }
  }

  public void getCallSiteInformation(SootMethod src, String methodName) {
    Body b = src.getActiveBody();
    // System.out.println(b.getLocalCount());
    // System.out.println("Line number " + b.getJavaSourceStartLineNumber());
    Chain<Local> locals = b.getLocals();
    PatchingChain units = b.getUnits();
    Iterator unitsIt = units.iterator();
    if (unitsIt == null) {
      System.out.println("NOUNITINFO");
    }
    while (unitsIt.hasNext()) {
      Unit unit = (Unit) unitsIt.next();
      if (unit instanceof InvokeStmt) {
        // System.out.println("HERE1");
        InvokeStmt iStmt = (InvokeStmt) unit;
        LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
        if (tag != null) {
          printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
              iStmt.getInvokeExpr(), methodName, tag.getLineNumber());
          // String string = unit.toString();
          // if (string.matches("\\s*.*virtualinvoke.*")) {
          // LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
          // System.out.println("line number with virtualinvoke: " + string + " at "
          // + tag.getLineNumber());
          // }
          //
          // if (unit instanceof InvokeStmt) {
          // }
        } else {
          System.out.println("SOMETHINGWRONG");
        }
      } else {
        // System.out.println("HERE2 " + unit.toString());
        if (unit instanceof Stmt) {
          // System.out.println("GOTCHA");
          Stmt stmt = (Stmt) unit;
          if (stmt.containsInvokeExpr()) {
            LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
            if (tag != null) {
              // System.out.println("HEREWEGO");
              printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  ((Stmt) unit).getInvokeExpr(), methodName, tag.getLineNumber());
            } else {
              System.out.println("SOMETHINGWRONG");
            }
            // System.out.println("GOTCHA");
          } else if (stmt instanceof InvokeStmt) {
            // System.out.println("INVOKESTMT");
          } else if (stmt instanceof JInvokeStmt) {
            // System.out.println("JINVOKESTMT");
          } else {

            // System.out.println("NOINVOKEEXPR");
          }
        }
        if (unit instanceof VirtualInvokeExpr) {
          // System.out.println("GOTCHA 2");
        }
        for (ValueBox vB : unit.getUseBoxes()) {
          if (vB.getValue() instanceof VirtualInvokeExpr) {
            LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
            if (tag != null) {
              printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  (VirtualInvokeExpr) vB.getValue(), methodName, tag.getLineNumber());
            } else {
              System.out.println("SOMETHINGWRONG");
            }
          } else {
            // System.out.println("FOUND " + vB.getValue().toString());
          }
        }
        for (UnitBox uB : unit.getUnitBoxes()) {
          Unit u = uB.getUnit();
          if (u instanceof InvokeStmt) {
            // System.out.println("HERE3");
            InvokeStmt iStmt = (InvokeStmt) u;
            LineNumberTag tag = (LineNumberTag) u.getTag("LineNumberTag");
            if (tag != null) {
              printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  iStmt.getInvokeExpr(), methodName, tag.getLineNumber());
            } else {
              System.out.println("SOMETHINGWRONG");
            }
          } else {
            // System.out.println("HERE 4 " + u);
          }
        }
      }
    }
  }

  Stack<SootMethod> sMStack = new Stack<SootMethod>();
  Stack<Integer> idStack = new Stack<Integer>();

  // public void buildPartICoG(CallGraph cg, int srcId, SootMethod target) {
  // if (!target.getSignature().contains(programPrefix)) {
  // return;
  // }
  // int targetId = graph.containsNode(target.getSignature());
  // if (targetId != -1) {
  // graph.updateOutIdForNode(targetId, srcId);
  // return;
  // } else {
  // CCGNode node = new CCGNode(target.getSignature());
  // node.addOutNodeId(srcId);
  // graph.addNewNode(node.getId(), node);
  // Iterator sources = new Sources(cg.edgesInto(target));
  // if (sources.hasNext()) {
  // while (sources.hasNext()) {
  // SootMethod parent = (SootMethod) sources.next();
  // if (parent.getSignature().contains(programPrefix)) {
  // buildPartICoG(cg, node.getId(), parent);
  // }
  // }
  // } else {
  // // graph.addStartingNode(target.getSignature(), node.getId());
  // return;
  // }
  // }
  // }

  final static int MAX_LEVEL = 0;

  void processExternalLibCall(CallGraph cg, String directParent, SootMethod target, int level) {
    if (level > MAX_LEVEL) {
      return;
    }
    Iterator sources = new Sources(cg.edgesInto(target));
    while (sources.hasNext()) {
      SootMethod parent = (SootMethod) sources.next();
      if (parent.getSignature().contains(programPrefix)) {
        System.out.println(level + " PARENT " + directParent + " CALLED-FROM "
            + parent.getSignature());
        return;
      }
      // else {
      // System.out.println(level + " TRANSITIVE " + directParent + " CALLED-FROM "
      // + parent.getSignature());
      // }
      processExternalLibCall(cg, directParent, parent, level + 1);
    }
  }

  public void buildPartICoGNoStack(CallGraph cg, int firstSrcId, SootMethod firstTarget) {
    System.out.println("buildPartICoGNoStack");
    if (!firstTarget.getSignature().contains(programPrefix)) {
      return;
    }

    sMStack.push(firstTarget);
    idStack.push(firstSrcId);
    while (!sMStack.empty()) {
      SootMethod target = sMStack.pop();
      int srcId = idStack.pop();

      int targetId = graph.containsNode(target.getSignature());
      System.out.println("Pop " + srcId + ": " + target.getSignature());
      if (targetId != -1) {
        graph.updateOutIdForNode(targetId, srcId);
        System.out.println("Visited");
        // return;
      } else {
        CCGNode node = new CCGNode(target.getSignature());
        node.addOutNodeId(srcId);
        graph.addNewNode(node.getId(), node);
        Iterator sources = new Sources(cg.edgesInto(target));
        boolean isStartingPoint = true;
        // StringBuilder srcSig = new StringBuilder("NULL"); // name of srcSig which calls a
        // starting
        // // point which can be JVM,
        // Thrift lib

        // if (sources.hasNext()) {
        while (sources.hasNext()) {
          SootMethod parent = (SootMethod) sources.next();
          // the value of srcSig might not be correct
          // srcSig.append(parent.getSignature() + "::::");
          if (!parent.getSubSignature().equals("void <clinit>()")) {
            if (parent.getSignature().contains(programPrefix)) {
              System.out.println("Push " + node.getId() + ": " + target.getSignature() + " - "
                  + parent.getSignature() + " : " + parent.getSubSignature());
              getCallSiteInformation(parent, target.getName());
              sMStack.push(parent);
              idStack.push(node.getId());
              isStartingPoint = false;
              // buildPartICoG(cg, node.getId(), parent);
            }
            // else {
            // System.out.println("Visited or external parent " + parent.getSignature());
            // if (!parent.getSignature().contains(programPrefix)) {
            // if (graph.containsNode(parent.getSignature()) == -1) {
            // CCGNode pNode = new CCGNode(parent.getSignature());
            // node.addOutNodeId(node.getId());
            // graph.addNewNode(pNode.getId(), pNode);
            // graph.addStartingNode(parent.getSignature(), pNode.getId());
            // System.out.println("Starting Point " + parent.getSignature());
            // } else {
            // System.out.println("WEIRD");
            // }
            // }
            // }
          } else {
            System.out.println("clinit");
          }
        }
        // }

        if (isStartingPoint) {
          // if (target.isEntryMethod()) {
          // srcSig.append(" ENTRY METHOD");
          // }
          graph.addStartingNode(target.getSignature(), "", node.getId());
          System.out.println("Starting Point " + target.getSignature());

          // if (!srcSig.equals("NULL")) { // we need to recursively process to see we can reach a
          // // point inside the host program
          // sources = new Sources(cg.edgesInto(target));
          // while (sources.hasNext()) {
          // SootMethod parent = (SootMethod) sources.next();
          // processExternalLibCall(cg, parent.getSignature(), parent, 0);
          // }
          //
          // }
          // return;
        }
      }
    }
  }


  public void analyseCallGraph(CallGraph cg, Set<String> classNames, String methodName) {
    countMatch = 0;
    SootMethod target = null;

    // Chain<SootClass> classes = Scene.v().getClasses();
    for (String className : classNames) {
      if (Scene.v().containsClass(className)) {
        // for (SootClass sClass : classes) {
        SootClass sClass = Scene.v().getSootClass(className);
        // System.out.println(sClass.getName());
        target = null;

        // if (className.contains(sClass.getName())) {
        try {
          target = sClass.getMethodByName(methodName);
        } catch (Exception e) {

        }
        // System.out.println("Found it 2");
        // for (Tag t : sClass.getTags()) {
        // // System.out.println(t.getName() + " : " + t.getValue());
        // if (t instanceof InnerClassTag) {
        // InnerClassTag innerClassTag = (InnerClassTag) t;
        // System.out.println("Inner " + innerClassTag.getInnerClass());
        // }
        // }

        // }
        // else {
        // for (SootMethod sM : sClass.getMethods()) {
        // if (sM.getName().equals(methodName))
        // target = sM;
        // }
        // }

        // SootMethod target = Scene.v().getMainClass().getMethodByName("start");

        if (target != null) {
          System.out.println("Checking " + target.getSignature());
          if (graph.containsNode(target.getSignature()) == -1) {
            CCGNode node = new CCGNode(target.getSignature());
            // node.addOutNodeId(Integer.MAX_VALUE);
            graph.addNewNode(node.getId(), node);
            Iterator sources = new Sources(cg.edgesInto(target));
            while (sources.hasNext()) {
              sMStack.clear();
              idStack.clear();
              SootMethod src = (SootMethod) sources.next();
              System.out.println("");
              System.out.println(target + " " + target.getParameterCount()
                  + " params might be called by " + src);

              // if (!src.getSubSignature().equals("void <clinit>()")) {
              // try {
              // buildPartICoGNoStack(cg, node.getId(), src);
              // // System.out.println("NOT HERE");
              // } catch (Exception e) {
              // e.printStackTrace();
              // }
              // }

              countMatch++;
              // getCallSiteInformation(src, methodName);

              // for (Local l : locals) {
              // // System.out.println("Local: " + l.getName());
              // }
              //
              // List<ValueBox> defBoxes = b.getDefBoxes();
              // for (int i = 0; i < defBoxes.size(); i++) {
              // // System.out.println("Def " + defBoxes.get(i).getValue() + " : "
              // // + defBoxes.get(i).getJavaSourceStartLineNumber());
              // List<Tag> tags = defBoxes.get(i).getTags();
              //
              // for (Tag tag : tags) {
              // // System.out.println(tag.getName() + " : " + tag.getValue());
              // // if (tag instanceof LineNumberTag) {
              // // LineNumberTag lineNumberTag = (LineNumberTag)tag;
              // // System.out.println(lineNumberTag.getLineNumber());
              // // }
              // }
              // }
              //
              // List<ValueBox> vBoxes = b.getUseBoxes();
              // for (int i = 0; i < vBoxes.size(); i++) {
              // Value v = vBoxes.get(i).getValue();
              // if (v instanceof VirtualInvokeExpr) {
              // LineNumberTag tag = (LineNumberTag) vBoxes.get(i).getTag("LineNumberTag");
              // if (tag != null) {
              // System.out.println("Value " + v + " : "
              // + vBoxes.get(i).getJavaSourceStartLineNumber());
              // VirtualInvokeExpr vI = (VirtualInvokeExpr) v;
              //
              // if (vI.getMethodRef().name().equals(methodName)) {
              // System.out.print(vI.getMethodRef().name() + ": ");
              // for (int j = 0; j < vI.getArgCount(); j++) {
              // System.out.print(vI.getArg(j) + ", ");
              // }
              // System.out.println();
              // }
              // for (Tag t : vBoxes.get(i).getTags()) {
              // System.out.println("TAG " + t.getName() + " : " + t.getValue());
              // }
              // }
              // }
              // }
              //
              // List<UnitBox> uBoxes = b.getAllUnitBoxes();
              // for (int i = 0; i < uBoxes.size(); i++) {
              // // System.out.println(uBoxes.get(i).getUnit().getJavaSourceStartLineNumber());
              // }
              // List<Tag> tags = target.getTags();
              // for (Tag tag : tags) {
              // // System.out.println(tag.getName() + " : " + tag.getValue());
              // // if (tag instanceof LineNumberTag) {
              // // LineNumberTag lineNumberTag = (LineNumberTag)tag;
              // // System.out.println(lineNumberTag.getLineNumber());
              // // }
              // }
            }
          } else {
            System.out.println("Analyzed " + target.getSignature());
          }
        }
        // }
      }
    }
    System.out.println("THERE ARE " + countMatch);
  }
}
