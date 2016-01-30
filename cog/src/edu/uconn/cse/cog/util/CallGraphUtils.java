package edu.uconn.cse.cog.util;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.queue.QueueReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
      try {
        // get method by subsignature
        method = c.getMethod(sPoint.split(":")[1].trim().replace(">", ""));
        if (method == null)
          System.err.println("NULL METHOD");
        System.out.println("method Sub Signature " + method.getSubSignature());

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
