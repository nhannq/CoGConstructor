package edu.uconn.cse.cog.constructor.ccg;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.Util;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.ReturnStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.queue.QueueReader;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HBaseCallGraphConstructor extends GraphBuilderAbstract {

  public static void main(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    HBaseCallGraphConstructor cassCG = new HBaseCallGraphConstructor();
    cassCG.initialize();
    // argsList.add("-w");
    Util.readFile(cassCG.libFile, argsList);
    // argsList.add("-allow-phantom-refs");
    // argsList.add("-p");
    // argsList.add("jb");
    // argsList.add("use-original-names:true");
    // argsList.add("verbose");
    argsList.add("-p");
    argsList.add("cg");
    argsList.add("all-reachable:true");
    // argsList.add("-time");
    argsList.add("-asm-backend");

    int server = 1;
    if (server == 1) {
      argsList.add("-p");
      argsList.add("cg.spark");
      argsList.add("enabled:true");
      argsList.add("-p");
      argsList.add("cg.spark");
      argsList.add("on-fly-cg:true");
      argsList.add("pre-jimplify:true");
    } else if (server == 2) {
      argsList.add("-p");
      argsList.add("cg.paddle");
      argsList.add("enabled:true");
    }

    argsList.add("use-original-names:true");
    // argsList.add("-process-dir");
    String mainClass = "org.apache.cassandra.service.CassandraDaemon";
    argsList.add("-main-class");
    argsList.add(mainClass);
    argsList.add(mainClass);

    args = argsList.toArray(new String[0]);

    System.out.println("JB " + PackManager.v().getPack("jb").getDefaultOptions());
    System.out.println("JBLS " + PackManager.v().getPhase("jb.ls").getDefaultOptions());
    System.out.println("JBDAE " + PackManager.v().getPhase("jb.dae").getDefaultOptions());
    System.out.println("CHCHA " + PackManager.v().getPhase("cg.cha").getDeclaredOptions());
    System.out.println("CG " + PackManager.v().getPack("cg").getDeclaredOptions());
    System.out.println(PackManager.v().getPhase("cg.spark").getDeclaredOptions());

    Options.v().parse(args);
    Options.v().set_keep_line_number(true);
    Options.v().set_whole_program(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_no_bodies_for_excluded(true);

    // add custom entry points
    // https://github.com/Sable/soot/wiki/Using-Soot-with-custom-entry-points
    List<String> customEntryPoints = new ArrayList<String>();
    Util.readFile(cassCG.startingPointFile, customEntryPoints);

    List addedEntryPoints = new ArrayList();
    for (String ePoint : customEntryPoints) {
      SootClass c = Scene.v().forceResolve(ePoint.split(":")[0], SootClass.BODIES);
      c.setApplicationClass();
      Scene.v().loadNecessaryClasses();
      SootMethod method = null;
      String methodName = ePoint.split(":")[1];
      if (!methodName.contains("(")) {
        try {
          method = c.getMethodByName(methodName);
        } catch (Exception e) {

        }
      } else {
        try {
          method = c.getMethod(methodName);
        } catch (Exception e) {

        }
      }
      if (method != null)
        addedEntryPoints.add(method);
      // System.err.println(method.getSignature()); //print to get the entry points with correct
      // format
    }
    Scene.v().setEntryPoints(addedEntryPoints);
    PackManager.v().runPacks();

    // printConfigurationAPIs();

    // String className = "org.apache.cassandra.cql3.QueryProcessor";
    // String methodSig =
    // "<org.apache.cassandra.cql3.QueryProcessor: org.apache.cassandra.transport.messages.ResultMessage processStatement(org.apache.cassandra.cql3.CQLStatement,org.apache.cassandra.service.QueryState,org.apache.cassandra.cql3.QueryOptions)>";
    // className = "org.apache.cassandra.io.sstable.SSTableRewriter$InvalidateKeys";
    // methodSig =
    // "<org.apache.cassandra.io.sstable.SSTableRewriter$InvalidateKeys: void <init>(org.apache.cassandra.io.sstable.SSTableReader,java.util.Collection)>";
    // CallGraphUtils.printTargets(className, methodSig);

    printEntryPoints();

    programPrefix = "org.apache.cassandra";
    externalInvocationMethods = "org.apache.cassandra.thrift.Cassandra";

    List<String> optionAPIS = new ArrayList<String>();
    Util.readFile(cassCG.optionAPIFile, optionAPIS);

    CallGraph cg = Scene.v().getCallGraph();
    Set<String> confClasses = new HashSet<String>() {
      {
        add("org.apache.cassandra.config.DatabaseDescriptor");
      }
    };

    if (cassCG.detectRechableMethod == 0) {
      int couldnotFind = 0;
      if (!cassCG.parseOneOption) {
        for (String oAPI : optionAPIS) {
          // String oAPI = "getAuthenticator";
          System.out.println("------------\n");
          System.out.println("Analyzing " + oAPI);
          if (cassCG.analyseCallGraph(cg, confClasses, oAPI) == 0) {
            couldnotFind++;
          }
          System.out.println("============\n");
          
        }
      } else {
        // String oAPI = "getAuthenticator";
        System.out.println("Analyzing " + cassCG.oAPI);
        cassCG.analyseCallGraph(cg, confClasses, cassCG.oAPI);
        System.out.println("============\n");
//        cassCG.graph.DFS();
      }
      
      System.out.println("CoundnotFind " + couldnotFind);
    } else {

      FileWriter fWriter = null;
      try {
        fWriter = new FileWriter(cassCG.rechableMethodFileName);
        List<String> reachableMethods = new ArrayList<String>();
        Util.readFile(cassCG.mightRechableMethodFileName, reachableMethods);
        for (String methodSig2 : reachableMethods) {
          System.out.println("Checking " + methodSig2);
          SootMethod sm2 = null;
          try {
            sm2 = Scene.v().getMethod(methodSig2);
          } catch (Exception e) {

          }
          if (sm2 != null) {
            // System.out.println("METHOD::: " + sm2.getSignature() + " at "
            // + sm2.getJavaSourceStartLineNumber());

            if (Scene.v().getReachableMethods().contains(sm2)) {
              // System.out.println("REACHABLEMETHOD");
              fWriter.write(methodSig2 + "\n");
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          fWriter.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      // print all real rechable methods
      if (cassCG.printAllRealRechableMethod == 1) {
        try {
          FileWriter f = new FileWriter(cassCG.reallyRechableMethodFileName);
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

    }

    // String testClass = "org.apache.cassandra.db.ColumnFamilyStore";
    // if (Scene.v().containsClass(testClass)) {
    // // System.out.println("FOUND org.apache.cassandra.tools.NodeTool$Join");
    // for (SootMethod sM : Scene.v().getSootClass(testClass).getMethods()) {
    // System.out
    // .println(sM.getName() + " : " + sM.getSignature() + " :: " + sM.getSubSignature());
    // }
    // }

    // for (SootClass sClass : Scene.v().getClasses()) {
    // System.out.println(sClass.getName());
    // }
    // cassCG.graph.printStartingNodes();

    // System.out.println("InitialSeeds");
    // Set<Unit> fUnit = cassCG.initialSeedData;
    // for (Unit u : fUnit) {
    // if (u instanceof InvokeExpr) {
    // InvokeExpr iExpr = (InvokeExpr) u;
    // System.out.println(iExpr.getMethod().getSignature() + " ::: "
    // + iExpr.getMethodRef().getSignature());
    // }
    // if (u instanceof InvokeStmt) {
    // InvokeStmt iStmt = (InvokeStmt) u;
    // InvokeExpr iExpr = iStmt.getInvokeExpr();
    // System.out.println(iExpr.getMethod().getSignature() + " ::: "
    // + iExpr.getMethodRef().getSignature());
    //
    // }
    // System.out.println(u);
    // }

    // IInfoflow infoflow = new Infoflow();
    // String appPath =
    // "/home/nqn12001/workspaceluna/apache-cassandra-2.1.8-src/build/classes/main/";
    // String libPath = "";
    //
    // // <soot.jimple.infoflow.test.TestNoMain: java.lang.String function1()>
    // List<String> entryPointList = new ArrayList<String>() {
    // {
    // add("<org.apache.cassandra.service.CassandraDaemon: void main(java.lang.String[])>");
    // }
    // };
    //
    // List<String> sources = new ArrayList<String>() {
    // {
    // // add("<test.Main: void main(java.lang.String[])>");
    // add("<org.apache.cassandra.config.DatabaseDescriptor: long getWriteRpcTimeout()>");
    // }
    // };
    //
    // List<String> sinks = new ArrayList<String>() {
    // {
    // // add("<test.Main: int foo2()>");
    // // add("<test.Main: void sink(java.lang.String)>");
    // add("<org.apache.cassandra.service.CassandraDaemon: void main(java.lang.String[])>");
    // // add("<test.Main: int secret()>");
    // }
    // };
    //
    // infoflow.computeInfoflow(appPath, "", entryPointList, sources, sinks);
  }

  static void printEntryPoints() {
    List<SootMethod> entryPoints = Scene.v().getEntryPoints();
    for (SootMethod sMethod : entryPoints) {
      System.out.println("EntryPoint::: " + sMethod.getName() + " " + sMethod.getSignature()
          + " of " + sMethod.getDeclaringClass().getName());
      // Iterator sources = new Sources(cg.edgesInto(sMethod));

      // while (sources.hasNext()) {
      // SootMethod src = (SootMethod) sources.next();
      // if (src.getDeclaringClass().getName().contains("org.apache"))
      // System.out.println(sMethod + " might be called by " + src);
      // }
      // break;
    }
  }

  static void printConfigurationAPIs() {
    SootClass optionLoadClass =
        Scene.v().getSootClass("org.apache.cassandra.config.DatabaseDescriptor");
    for (SootMethod m : optionLoadClass.getMethods()) {
      System.out.println(m.getName() + " RETURNS " + m.getReturnType());
      if (m.hasActiveBody()) {
        Body b = m.getActiveBody();
        for (Unit u : b.getUnits()) {
          System.out.println(u);
          if (u instanceof ReturnStmt) {
            ReturnStmt rStmt = (ReturnStmt) u;
            System.out.println(rStmt);
          }
        }
        System.out.println("======");
      }
    }
  }
}
