package edu.uconn.cse.cog.constructor.ccg;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.CallGraphUtils;
import edu.uconn.cse.cog.util.Util;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.ReturnStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CassandraCallGraphConstructor extends GraphBuilderAbstract {
  static CassandraCallGraphConstructor cassCG;

  public static void main(String[] args) {
    List<String> argsList = new ArrayList<String>(Arrays.asList(args));
    cassCG = new CassandraCallGraphConstructor();
    cassCG.initialize();
    Util.readFile(cassCG.libFile, argsList);
    argsList.add("-p");
    argsList.add("cg");
    argsList.add("all-reachable:true");
    argsList.add("-asm-backend");

    int server = 1;
    if (server == 1) { // using spark
      argsList.add("-p");
      argsList.add("cg.spark");
      argsList.add("enabled:true");
      argsList.add("-p");
      argsList.add("cg.spark");
      argsList.add("on-fly-cg:true");
      argsList.add("pre-jimplify:true");
    } else if (server == 2) { // using paddle
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
    Options.v().set_whole_program(true); // "-w"
    Options.v().set_allow_phantom_refs(true); // "-allow-phantom-refs"
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_no_bodies_for_excluded(true);

    // add custom entry points
    // https://github.com/Sable/soot/wiki/Using-Soot-with-custom-entry-points


    List addedStartingPoints = CallGraphUtils.getAddedStartingPointList(cassCG.startingPointFile);

    Scene.v().setEntryPoints(addedStartingPoints);
    PackManager.v().runPacks();

    CallGraphUtils.printStartingPoints();

    cassCG.constructGraph();

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
    System.out.println("Done " + CassandraCallGraphConstructor.class.getName());
  }

  private void constructGraph() {
    programPrefix = "org.apache.cassandra";
    externalInvocationMethods = "org.apache.cassandra.thrift.Cassandra";

    List<String> optionAPIS = new ArrayList<String>();
    Util.readFile(cassCG.optionAPIFile, optionAPIS);

    CallGraph cg = Scene.v().getCallGraph();
    Set<String> confClasses = new HashSet<String>() {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      {
        add("org.apache.cassandra.config.DatabaseDescriptor");
      }
    };

    if (detectRechableMethod == 0) {
      int couldnotFind = 0;
      try {
        generalInfoFW = new FileWriter("Cassandra" + version + ".txt");
        if (!cassCG.parseOneOption) {
          for (String oAPI : optionAPIS) {
            System.out.println("------------\n");
            System.out.println("Analyzing " + oAPI);
            generalInfoFW.write(oAPI + "\n");
            int rs = analyseCallGraph(cg, confClasses, oAPI);
            if (rs == 0) {
              couldnotFind++;
            }

            System.out.println("============\n");
            generalInfoFW.write(rs + "\n");
            generalInfoFW.write("==========\n");
          }
        } else {
          System.out.println("Analyzing " + oAPI);
          analyseCallGraph(cg, confClasses, oAPI);
          System.out.println("============\n");
          // cassCG.graph.DFS();
        }

        System.out.println("CoundnotFind " + couldnotFind);
        cassCG.generalInfoFW.write("NB Settings  " + optionAPIS.size());
        cassCG.generalInfoFW.close();
      } catch (Exception e) {

      }
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
        CallGraphUtils.printReachableMethods(cassCG.rechableMethodFileName);
      }

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
