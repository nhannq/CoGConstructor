package edu.uconn.cse.cog.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.queue.QueueReader;

public class CallGraphUtils {
  // print the information of which invocation is made inside the method methodSig
  public static void printTargets(String className, String methodSig) {
    CallGraph cg = Scene.v().getCallGraph();
    SootClass cl = Scene.v().getSootClass(className);
    for (SootMethod sm : cl.getMethods()) {
      if (sm.getSignature().equals(methodSig)) {

        // Iterator sources = new Sources(cg.edgesOutOf(sm));
        // while (sources.hasNext()) {
        // SootMethod sm2 = (SootMethod) sources.next();
        // System.out.println("CALLS " + sm2.getSignature());
        // }
        if (sm.hasActiveBody()) {
          System.out.println("HASACTIVEBODY");
        }
        Body b = sm.getActiveBody();

        for (Unit u : b.getUnits()) {
          System.out.println(u);

          Iterator targets = new Targets(cg.edgesOutOf(u));
          while (targets.hasNext()) {
            SootMethod sm2 = (SootMethod) targets.next();
            System.out.println("CALLS " + sm2.getSignature());
          }

        }
      }
    }
  }

  public static void printStartingPoints() {
    CallGraph cg = Scene.v().getCallGraph();
    List<SootMethod> entryPoints = Scene.v().getEntryPoints();
    for (SootMethod sMethod : entryPoints) {
      System.out.println("StaringPoint " + sMethod.getName() + " " + sMethod.getSignature()
          + " of " + sMethod.getDeclaringClass().getName());
      Iterator sources = new Sources(cg.edgesInto(sMethod));

      while (sources.hasNext()) {
        SootMethod src = (SootMethod) sources.next();
        if (src.getDeclaringClass().getName().contains("org.apache"))
          System.out.println(sMethod + " might be called by " + src);
      }
    }
  }

  public static void printReachableMethods(String fileName) {
    try {
      FileWriter f = new FileWriter(fileName);
      QueueReader<MethodOrMethodContext> reallyRechableMethods =
          Scene.v().getReachableMethods().listener();
      while (reallyRechableMethods.hasNext()) {
        MethodOrMethodContext m = reallyRechableMethods.next();
        f.write(m.method().getSignature() + "\n");
      }
      f.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public static boolean checkReachableMethods(String className, String methodName) {
    CallGraph cg = Scene.v().getCallGraph();
    SootClass sClass = null;
    try {
      sClass = Scene.v().loadClassAndSupport(className); // .getSootClass(className);
    } catch (Exception e) {

    }
    if (sClass == null) {
      System.out.println("Class " + className + " doesn't exist");
      return false;
    }
    sClass.setApplicationClass();
    System.out.println("checkReachableMethods " + sClass.getName());
    SootMethod sm = null;

    // for (SootMethod sm : sClass.getMethods()) {
    // if (sm.hasActiveBody())
    // System.out.println(sm.getSignature());
    // }

    // if (className.contains(sClass.getName())) {
    try {
      // example: getInt() in Hadoop
      sm = sClass.getMethodByName(methodName);
      if (sm == null) {
        System.out.println("Method " + methodName + " is null");
        return false;
      }

      if (!sm.hasActiveBody()) {
        System.out.println("METHOD " + methodName + " does not have active body");
        // return false;
      } else
        System.out.println("FINDTHISWEIREDMETHOD");
      Iterator<Edge> edges = cg.edgesInto(sm);
      while (edges.hasNext()) {
        Edge e = edges.next();
        Stmt stmt = e.srcStmt();
        if (stmt.containsInvokeExpr())
          // for (int i = 0; i < stmt.getInvokeExpr().getArgCount(); ++i)
          if (stmt.getInvokeExpr().getArgCount() >= 1)
            System.out.println("Params " + "\t"
                + stmt.getInvokeExpr().getArg(0).toString().replaceAll("\"", "").trim() + " at "
                + stmt.getJavaSourceStartLineNumber() + "\n");
      }
      Body b = sm.getActiveBody();
      System.out.println(b.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  public static List getAddedStartingPointList(String startingPointFile) {
    List<String> customStartingPoints = new ArrayList<String>();
    List addedStartingPoints = new ArrayList();
    Util.readFile(startingPointFile, customStartingPoints);

    for (String sPoint : customStartingPoints) {
      String className = sPoint.split(":")[0].replace("<", "");
      SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
      c.setApplicationClass();
      Scene.v().loadNecessaryClasses();
      SootMethod method = null;
      String fullMethodName = sPoint.split(":")[1].trim().split("\\s+")[1].trim();
      String methodName = fullMethodName.substring(0, fullMethodName.indexOf("("));
      // System.out.println(methodName);
      try {
        if (!methodName.contains("(")) {
          method = c.getMethodByName(methodName);
          if (method == null)
            System.err.println("NULLMETHOD\t" + sPoint);
          // else
          // System.out.println("method Sub Signature " + method.getSubSignature());
        } else {
          // get method by subsignature
          method = c.getMethod(sPoint.split(":")[1].trim().replace(">", ""));
          // if (method == null)
          // System.err.println("NULL METHOD");
          // System.out.println("method Sub Signature " + method.getSubSignature());
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

      if (method != null)
        addedStartingPoints.add(method);
      // System.err.println(method.getSignature()); //print to get the entry points with correct
      // format
    }
    return addedStartingPoints;
  }

}
