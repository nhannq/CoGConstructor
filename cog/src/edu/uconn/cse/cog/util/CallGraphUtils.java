package edu.uconn.cse.cog.util;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.Iterator;

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
}
