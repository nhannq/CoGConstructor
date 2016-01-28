package edu.uconn.cse.cog.constructor.ccg;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.Util;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HadoopCallGraphConstructor extends GraphBuilderAbstract {

  // public static void analyseCallGraph(CallGraph cg, Set<String> className, String methodName) {
  // SootMethod target = null;
  // Chain<SootClass> classes = Scene.v().getClasses();
  //
  // for (SootClass sClass : classes) {
  // // System.out.println(sClass.getName());
  // target = null;
  // if (sClass.getName().equals("org.apache.hadoop.mapred.MapTask")) {
  // if (sClass.isPhantom() || sClass.isPhantomClass())
  // System.out.println("HERE WE ARE ARE");
  //
  // for (SootMethod m : sClass.getMethods()) {
  // System.out.println("METHOD " + m.getName());
  // if (m.hasActiveBody()) {
  // List<ValueBox> vBoxes = m.getActiveBody().getUseBoxes();
  // for (int i = 0; i < vBoxes.size(); i++) {
  // Value v = vBoxes.get(i).getValue();
  // if (v instanceof VirtualInvokeExpr) {
  // // System.out.println("Value " + v + " : " +
  // // vBoxes.get(i).getJavaSourceStartLineNumber());
  // VirtualInvokeExpr vI = (VirtualInvokeExpr) v;
  // if (vI.getMethodRef().name().equals(methodName)) {
  // System.out.print(vI.getMethodRef().name() + ": ");
  // for (int j = 0; j < vI.getArgCount(); j++) {
  // System.out.print(vI.getArg(j) + ", ");
  // }
  // System.out.println();
  // }
  // }
  // }
  // }
  // }
  // }
  // if (className.contains(sClass.getName())) {
  // target = sClass.getMethodByName(methodName);
  // System.out.println("Found it 2");
  // }
  // // else {
  // // for (SootMethod sM : sClass.getMethods()) {
  // // if (sM.getName().equals(methodName))
  // // target = sM;
  // // }
  // // }
  //
  // // SootMethod target = Scene.v().getMainClass().getMethodByName("start");
  //
  // if (target != null) {
  // Iterator sources = new Sources(cg.edgesInto(target));
  // while (sources.hasNext()) {
  // SootMethod src = (SootMethod) sources.next();
  // System.out.println(target + " " + target.getParameterCount()
  // + " params might be called by " + src);
  // countMatch++;
  // Body b = src.getActiveBody();
  // // System.out.println(b.getLocalCount());
  // // System.out.println("Line number " + b.getJavaSourceStartLineNumber());
  // Chain<Local> locals = b.getLocals();
  // PatchingChain units = b.getUnits();
  // Iterator unitsIt = units.iterator();
  // if (unitsIt == null) {
  // System.out.println("NOUNITINFO");
  // }
  // while (unitsIt.hasNext()) {
  // Unit unit = (Unit) unitsIt.next();
  // if (unit instanceof InvokeStmt) {
  // System.out.println("HERE1");
  // InvokeStmt iStmt = (InvokeStmt) unit;
  // LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
  // if (tag != null) {
  // printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
  // iStmt.getInvokeExpr(), methodName, tag.getLineNumber());
  // // String string = unit.toString();
  // // if (string.matches("\\s*.*virtualinvoke.*")) {
  // // LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
  // // System.out.println("line number with virtualinvoke: " + string + " at "
  // // + tag.getLineNumber());
  // // }
  // //
  // // if (unit instanceof InvokeStmt) {
  // // }
  // } else {
  // System.out.println("SOMETHINGWRONG");
  // }
  // } else {
  // System.out.println("HERE2 " + unit.toString());
  // if (unit instanceof Stmt) {
  // System.out.println("GOTCHA");
  // if (((Stmt) unit).containsInvokeExpr()) {
  // LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
  // if (tag != null) {
  // // printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
  // // (VirtualInvokeExpr) ((Stmt) unit).getInvokeExpr(), methodName,
  // // tag.getLineNumber());
  // } else {
  // System.out.println("SOMETHINGWRONG");
  // }
  // // System.out.println("GOTCHA");
  // }
  // }
  // if (unit instanceof VirtualInvokeExpr) {
  // System.out.println("GOTCHA 2");
  // }
  // for (ValueBox vB : unit.getUseBoxes()) {
  // if (vB.getValue() instanceof VirtualInvokeExpr) {
  // LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
  // if (tag != null) {
  // printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
  // (VirtualInvokeExpr) vB.getValue(), methodName, tag.getLineNumber());
  // } else {
  // System.out.println("SOMETHINGWRONG");
  // }
  // } else {
  // // System.out.println("FOUND " + vB.getValue().toString());
  // }
  // }
  // for (UnitBox uB : unit.getUnitBoxes()) {
  // Unit u = uB.getUnit();
  // if (u instanceof InvokeStmt) {
  // System.out.println("HERE3");
  // InvokeStmt iStmt = (InvokeStmt) u;
  // LineNumberTag tag = (LineNumberTag) u.getTag("LineNumberTag");
  // if (tag != null) {
  // printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
  // iStmt.getInvokeExpr(), methodName, tag.getLineNumber());
  // } else {
  // System.out.println("SOMETHINGWRONG");
  // }
  // } else {
  // System.out.println("HERE 4 " + u);
  // }
  // }
  // }
  // }
  //
  // // for (Local l : locals) {
  // // // System.out.println("Local: " + l.getName());
  // // }
  // //
  // // List<ValueBox> defBoxes = b.getDefBoxes();
  // // for (int i = 0; i < defBoxes.size(); i++) {
  // // // System.out.println("Def " + defBoxes.get(i).getValue() + " : "
  // // // + defBoxes.get(i).getJavaSourceStartLineNumber());
  // // List<Tag> tags = defBoxes.get(i).getTags();
  // //
  // // for (Tag tag : tags) {
  // // // System.out.println(tag.getName() + " : " + tag.getValue());
  // // // if (tag instanceof LineNumberTag) {
  // // // LineNumberTag lineNumberTag = (LineNumberTag)tag;
  // // // System.out.println(lineNumberTag.getLineNumber());
  // // // }
  // // }
  // // }
  // //
  // // List<ValueBox> vBoxes = b.getUseBoxes();
  // // for (int i = 0; i < vBoxes.size(); i++) {
  // // Value v = vBoxes.get(i).getValue();
  // // if (v instanceof VirtualInvokeExpr) {
  // // LineNumberTag tag = (LineNumberTag) vBoxes.get(i).getTag("LineNumberTag");
  // // if (tag != null) {
  // // System.out.println("Value " + v + " : "
  // // + vBoxes.get(i).getJavaSourceStartLineNumber());
  // // VirtualInvokeExpr vI = (VirtualInvokeExpr) v;
  // //
  // // if (vI.getMethodRef().name().equals(methodName)) {
  // // System.out.print(vI.getMethodRef().name() + ": ");
  // // for (int j = 0; j < vI.getArgCount(); j++) {
  // // System.out.print(vI.getArg(j) + ", ");
  // // }
  // // System.out.println();
  // // }
  // // for (Tag t : vBoxes.get(i).getTags()) {
  // // System.out.println("TAG " + t.getName() + " : " + t.getValue());
  // // }
  // // }
  // // }
  // // }
  // //
  // // List<UnitBox> uBoxes = b.getAllUnitBoxes();
  // // for (int i = 0; i < uBoxes.size(); i++) {
  // // // System.out.println(uBoxes.get(i).getUnit().getJavaSourceStartLineNumber());
  // // }
  // // List<Tag> tags = target.getTags();
  // // for (Tag tag : tags) {
  // // // System.out.println(tag.getName() + " : " + tag.getValue());
  // // // if (tag instanceof LineNumberTag) {
  // // // LineNumberTag lineNumberTag = (LineNumberTag)tag;
  // // // System.out.println(lineNumberTag.getLineNumber());
  // // // }
  // // }
  // }
  // }
  // }
  // System.out.println("THERE ARE " + countMatch);
  // }

  public static void main(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    // argsList.add("-w");
    Util.readFile("data/hadoop.txt", argsList);
    // argsList.add("-allow-phantom-refs");
    // argsList.add("-p");
    // argsList.add("jb");
    // argsList.add("use-original-names:true");
    // argsList.add("verbose");
    argsList.add("-p");
    argsList.add("cg");
    argsList.add("all-reachable:true");
    // argsList.add("-time");

    // argsList.add("-p");
    // argsList.add("cg.spark");
    // argsList.add("on-fly-cg:true");

    // argsList.add("-keep-line-number");
    // argsList.add("-use-original-names:true");
    // argsList.add("-process-dir");
    // argsList
    // .add("/home/nnguyen/workspaceluna/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/target/classes");
    argsList.add("-main-class");
    argsList.add("org.apache.hadoop.mapred.JobClient");
    argsList.add("org.apache.hadoop.mapred.JobClient");
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
    Util.readFile("data/hadoopEntryPoints.txt", customEntryPoints);

    // add custom entry points
    Options.v().parse(args);
    Options.v().set_keep_line_number(true);
    Options.v().set_whole_program(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");

    // Options.v().setPhaseOption("cg.spark", "on-fly-cg:true"); //this does not work, need to set
    // this option directly by using command line

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

    // ContextSensitiveCallGraph cscg = Scene.v().getContextSensitiveCallGraph(); //need to use
    // Spark or Paddle

    CallGraph cg = Scene.v().getCallGraph();
    // SootMethod target = Scene.v().getMethod("applyConfig");
    System.out.println("CG Size " + cg.size());
    // if (Scene.v().containsClass("org.apache.cassandra.config.DatabaseDescriptor")) {
    // System.out.println("Found it");
    // }

    Set<String> confClasses = new HashSet<String>() {
      {
        add("org.apache.hadoop.conf.Configuration");
        // add("org.apache.hadoop.mapred.JobConf");
      }
    };

    // analyseCallGraph(cg, "org.apache.hadoop.conf.Configuration", "getStrings");
    // <org.apache.hadoop.conf.Configuration: java.lang.String[] getStrings(java.lang.String)>
    //
    // analyseCallGraph(cg, "org.apache.cassandra.config.DatabaseDescriptor",
    // "getCounterCacheSavePeriod");


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
