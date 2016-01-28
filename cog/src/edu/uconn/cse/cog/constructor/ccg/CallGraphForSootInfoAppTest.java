package edu.uconn.cse.cog.constructor.ccg;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.cfg.DefaultBiDiICFGFactory;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.jimple.toolkits.ide.exampleproblems.IFDSReachingDefinitions;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CallGraphForSootInfoAppTest {
  public static void main(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    argsList.addAll(Arrays.asList(new String[] {"-w",// "-p", "cg.spark", "on-fly-cg:true",
        "-main-class", "edu.uconn.cse.appinfo.test.Main",// main-class
        "edu.uconn.cse.appinfo.test.Main",// argument classes
    // "testers.A" //
        }));
    // argsList.add("-soot-class-path");
    // argsList
    // .add("/home/nnguyen/workspaceluna/CoGConstructor/bin:/usr/java/jdk1.7.0_79/jre/lib/rt.jar:/usr/java/jdk1.7.0_79/jre/lib/jce.jar");
    argsList.add("-p");
    argsList.add("cg");
    argsList.add("all-reachable:true");

    argsList.add("-p");
    argsList.add("cg.spark");
    argsList.add("on-fly-cg:true");
    argsList.add("-f");
    argsList.add("J");
    // argsList.add("-no-bodies-for-excluded");
    // argsList.add("pre-jimplify:true");

    System.out.println("JB " + PackManager.v().getPack("jb").getDefaultOptions());
    System.out.println("JBLS " + PackManager.v().getPhase("jb.ls").getDefaultOptions());
    System.out.println("JBDAE " + PackManager.v().getPhase("jb.dae").getDefaultOptions());
    System.out.println("CHCHA " + PackManager.v().getPhase("cg.cha").getDeclaredOptions());
    System.out.println("CG" + PackManager.v().getPack("cg").getDeclaredOptions());

    // PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
    // // System.out.println(PackManager.v().getPhase("cg.spark").getDeclaredOptions());
    //
    // // PackManager.v().getPack("cg").add(new Transform("cg.myTrans", new SceneTransformer() {
    //
    // @Override
    // protected void internalTransform(String phaseName, Map options) {
    // CHATransformer.v().transform();
    // SootClass a = Scene.v().getSootClass("testers.A");
    //
    // SootMethod src = Scene.v().getMainClass().getMethodByName("doStuff");
    // CallGraph cg = Scene.v().getCallGraph();
    //
    // Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(src));
    //
    // while (targets.hasNext()) {
    // SootMethod tgt = (SootMethod) targets.next();
    // System.out.println(src + " may call " + tgt);
    // }
    //
    // Iterator<MethodOrMethodContext> sources = new Sources(cg.edgesInto(src));
    //
    // while (sources.hasNext()) {
    // SootMethod tgt = (SootMethod) sources.next();
    //
    // System.out.println(src + " may be called by " + tgt + " " + tgt.getName());
    // }
    //
    // }
    //
    // }));



    args = argsList.toArray(new String[0]);
    Options.v().parse(args); // disable if want to use soot.Main.main
    Options.v().set_keep_line_number(true);
    Options.v().set_whole_program(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");

    // soot.Main.main(args);

    List addedEntryPoints = new ArrayList();
    // for (String ePoint : customEntryPoints) {
    SootClass c = Scene.v().forceResolve("edu.uconn.cse.appinfo.test.Main", SootClass.BODIES);
    c.setApplicationClass();
    Scene.v().loadNecessaryClasses();
    SootMethod method;
    String methodName = "main";
    if (!methodName.contains("(")) {
      method = c.getMethodByName(methodName);
      System.out.println("FOUND IT");
    } else {
      // List<Type> types = new ArrayList<Type>();

      method = c.getMethod(methodName);
      System.out.println("FOUND IT");
    }
    addedEntryPoints.add(method);
    // }
    Scene.v().setEntryPoints(addedEntryPoints);
    PackManager.v().runPacks();

    JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
    IFDSTabulationProblem<Unit, Pair<Value, Set<DefinitionStmt>>, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem =
        (IFDSTabulationProblem) new IFDSReachingDefinitions(icfg);
    System.out.println("Number of threads :" + problem.numThreads());

    JimpleIFDSSolver<Pair<Value, Set<DefinitionStmt>>, InterproceduralCFG<Unit, SootMethod>> solver =
        new JimpleIFDSSolver<Pair<Value, Set<DefinitionStmt>>, InterproceduralCFG<Unit, SootMethod>>(
            problem, true);

    System.out.println("Started Solving....");
    System.out.println(Scene.v().getMainMethod().getName());
    SootMethod mMethod =
        Scene.v().getSootClass("edu.uconn.cse.appinfo.test.Main").getMethodByName("main");
    // SootMethod mMethod = Scene.v().getMainClass().getMethodByName("doStuff2");
    BiDirICFGFactory icfgFactory = new DefaultBiDiICFGFactory();
    IInfoflowCFG iCfg = icfgFactory.buildBiDirICFG(CallgraphAlgorithm.AutomaticSelection, true);

    CallGraph cg = Scene.v().getCallGraph();
    // SootClass cl = Scene.v().getMainClass();
    for (SootClass cl : Scene.v().getClasses()) {
      for (SootMethod sm : cl.getMethods()) {
        if (sm.getSignature().contains("edu.uconn.cse.appinfo.test.")) {
          // if
          // (sm.getSignature().equals("<testers.CallGraphs$CellIterator: java.lang.Object next()>"))
          // {
          // if (sm.getSignature().equals("<testers.CallGraphs: void p4(int)>")) {
          System.out.println("WE FOUND " + sm.getSignature() + " AT "
              + sm.getJavaSourceStartLineNumber());
          Iterator sources = new Sources(cg.edgesInto(sm));
          while (sources.hasNext()) {
            SootMethod sm2 = (SootMethod) sources.next();
            System.out.println("CALLED BY " + sm2.getSignature());
          }

          // sources = new Sources(cg.edgesOutOf(sm));
          // while (sources.hasNext()) {
          // SootMethod sm2 = (SootMethod) sources.next();
          // System.out.println("CALLS " + sm2.getSignature());
          // }
          // }
        }
      }
    }

    if (mMethod.hasActiveBody()) {
      System.out.println("YESSSSSSSSSSSSSSSSS");
      Body b = mMethod.getActiveBody();
      for (Unit u : b.getUnits()) {
        System.out.println(u + " AT " + u.getJavaSourceStartLineNumber());
      }
      System.out.println("===================");
      for (Unit u : b.getUnits()) {

        if (u instanceof IfStmt) {
          System.out.println("IF STMT AT " + u.getJavaSourceStartLineNumber());
          IfStmt stmt = (IfStmt) u;
          System.out.println(u);
          Value condition = stmt.getCondition();
          ValueBox conditionBox = stmt.getConditionBox();
          System.out.println("Condition " + condition + " AT "
              + conditionBox.getJavaSourceStartLineNumber());
          System.out.println("Target " + stmt.getTarget() + " AT "
              + stmt.getTarget().getJavaSourceStartLineNumber());
          System.out
              .println(iCfg.getPostdominatorOf(u) + " AT " + u.getJavaSourceStartLineNumber());

          System.out.println(iCfg.getSuccsOf(u));

          System.out.println("=====");
        }


        if (u instanceof GotoStmt) {
          System.out.println("GOTO STMT AT " + u.getJavaSourceStartLineNumber());
          GotoStmt stmt = (GotoStmt) u;
          System.out.println(u);
          System.out.println(stmt.getTarget());
          System.out.println("=====");
        }
      }
    }


    // commented to test the IfStmt
    // if (!Scene.v().getMainMethod().hasActiveBody()) {
    // System.out.println("NONOOOOOOOOOOOOOOOOOOOOOOOOOOO");
    // }
    // // Scene.v().getMainMethod().getActiveBody()
    // solver.solve();
    // System.out.println("Completed Solving....");
    // solver.dumpResults();

  }
}
