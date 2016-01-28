package edu.uconn.cse.cog.constructor.ccg;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.Util;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.options.Options;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class OldCassandraCallGraph extends GraphBuilderAbstract {

  public static void analyseCallGraph(CallGraph cg, String className, String methodName) {
    SootMethod target = null;
    Chain<SootClass> classes = Scene.v().getClasses();

    for (SootClass sClass : classes) {
      if (sClass.getName().equals(className)) {

        target = sClass.getMethodByName(methodName);
        System.out.println("Found it 2");
      }
    }

    // SootMethod target = Scene.v().getMainClass().getMethodByName("start");
    if (target != null) {
      Iterator sources = new Sources(cg.edgesInto(target));
      while (sources.hasNext()) {
        SootMethod src = (SootMethod) sources.next();
        System.out.println(target + " might be called by " + src);
      }
    }

  }

  public static void main(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    argsList.add("-w");
    Util.readFile("data/cass.txt", argsList);
    argsList.add("-allow-phantom-refs");
    argsList.add("-main-class");
    argsList.add("org.apache.cassandra.service.CassandraDaemon");
    argsList.add("org.apache.cassandra.service.CassandraDaemon");
    // argsList.add("org.apache.cassandra.cli.CliMain");
    // argsList.add("org.apache.cassandra.cli.CliMain");
    // argsList.addAll(Arrays.asList(new String[] {
    // "-w",
    // // "-process-path", "/home/nnguyen/workspaceluna/bin/apache-cassandra-2.1.8/lib/",
    // // "-soot-class-path",
    // "/home/nnguyen/workspaceluna/apache-cassandra-2.1.8-src/build/classes/main",
    // "-allow-phantom-refs",
    // // "-p", "cg", "all-reachable:true",
    // // "-p", "cg.spark", "on-fly-cg:true",
    // "-main-class", "org.apache.cassandra.service.CassandraDaemon", // main-class
    // "org.apache.cassandra.service.CassandraDaemon",// argument classes
    // "testers.A" //
    // }));

    System.out.println("JB " + PackManager.v().getPack("jb").getDefaultOptions());
    System.out.println("JBLS " + PackManager.v().getPhase("jb.ls").getDefaultOptions());
    System.out.println("JBDAE " + PackManager.v().getPhase("jb.dae").getDefaultOptions());
    System.out.println("CHCHA " + PackManager.v().getPhase("cg.cha").getDeclaredOptions());
    System.out.println("CG " + PackManager.v().getPack("cg").getDeclaredOptions());
    // PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTrans", new SceneTransformer() {
    System.out.println(PackManager.v().getPhase("cg.spark").getDeclaredOptions());
    // PackManager.v().getPack("cg").add(new Transform("cg.myTrans", new SceneTransformer() {
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

    List<String> customEntryPoints = new ArrayList<String>();
    Util.readFile("data/cassEntryPoints.txt", customEntryPoints);

    // add custom entry points
    Options.v().parse(args);
    List addedEntryPoints = new ArrayList();
    for (String ePoint : customEntryPoints) {
      SootClass c = Scene.v().forceResolve(ePoint.split(":")[0], SootClass.BODIES);
      c.setApplicationClass();
      Scene.v().loadNecessaryClasses();
      SootMethod method = c.getMethodByName(ePoint.split(":")[1]);
      addedEntryPoints.add(method);
    }
    Scene.v().setEntryPoints(addedEntryPoints);
    PackManager.v().runPacks();

    // soot.Main.main(args);

    CallGraph cg = Scene.v().getCallGraph();
    // SootMethod target = Scene.v().getMethod("applyConfig");
    System.out.println("CG Size " + cg.size());
    // if (Scene.v().containsClass("org.apache.cassandra.config.DatabaseDescriptor")) {
    // System.out.println("Found it");
    // }

    analyseCallGraph(cg, "org.apache.cassandra.service.CacheService",
        "getCounterCacheSavePeriodInSeconds");

    analyseCallGraph(cg, "org.apache.cassandra.config.DatabaseDescriptor",
        "getCounterCacheSavePeriod");


    List<SootMethod> entryPoints = Scene.v().getEntryPoints();
    for (SootMethod sMethod : entryPoints) {
      // System.out.println("EntryPoint " + sMethod.getName() + " " + sMethod.getSignature() +
      // " of "
      // + sMethod.getDeclaringClass().getName());
      // Iterator sources = new Sources(cg.edgesInto(sMethod));

      // while (sources.hasNext()) {
      // SootMethod src = (SootMethod) sources.next();
      // if (src.getDeclaringClass().getName().contains("org.apache"))
      // System.out.println(sMethod + " might be called by " + src);
      // }
      // break;
    }
  }
}
