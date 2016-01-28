package edu.uconn.cse.cog.constructor;

import edu.uconn.cse.cog.model.CCGNode;
import edu.uconn.cse.cog.model.CCGraph;
import edu.uconn.cse.cog.util.Util;

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
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.Sources;
import soot.tagkit.LineNumberTag;
import soot.util.Chain;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

public class GraphBuilderAbstract<T> {

  public static int countMatch;
  protected String libFile;
  public String mainClass;
  public String entryPointFile;
  public String optionAPIFile;
  public boolean parseOneOption;
  public String oAPI;
  public String currentOptionAPI;
  public String mightRechableMethodFileName;
  public String rechableMethodFileName;
  public String reallyRechableMethodFileName;
  public int detectRechableMethod;
  public int printAllRealRechableMethod;
  public String version;

  public CCGraph graph;

  protected static String programPrefix;
  // protected static Set<String> externalInvocationMethods = new HashSet<String>();
  protected static String externalInvocationMethods = null;

  Set<Unit> initialSeedData = new HashSet<Unit>();

  public void initialize() {
    libFile = "data/cass.txt";
    entryPointFile = "data/cassPIIEntryPoints.txt";
    String startingPointFile = "data/startingPoint.txt";
    parseOneOption = false;
    oAPI = "getAuthenticator";
    version = "1.0.0";
    String programPrefix = null;

    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream("ccg.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      mainClass = prop.getProperty("mainClass");
      libFile = prop.getProperty("libFile");
      entryPointFile = prop.getProperty("entryPointFile");
      optionAPIFile = prop.getProperty("optionAPIFile");
      if (Integer.parseInt(prop.getProperty("parseOneOption")) == 1) {
        parseOneOption = true;
        oAPI = prop.getProperty("optionAPI");
      }
      version = prop.getProperty("version");
      programPrefix = prop.getProperty("programPrefix");
      detectRechableMethod = Integer.parseInt(prop.getProperty("detectRechableMethod"));
      mightRechableMethodFileName =
          prop.getProperty("mightRechableMethodFileName") + version + ".txt";
      rechableMethodFileName = prop.getProperty("rechableMethodFileName") + version + ".txt";
      reallyRechableMethodFileName =
          prop.getProperty("reallyRechableMethodFileName") + version + ".txt";
      printAllRealRechableMethod = Integer.parseInt(prop.getProperty("printAllRealRechableMethod"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("VERSION " + version);
    System.out.println("METHOD " + oAPI);// Util.getMethodName(optionSource));
    System.out.println("mainClass " + mainClass);
    System.out.println("libFile " + libFile);



  }


  public boolean printInvokeStmtLineNumber(String callsiteInfo, InvokeExpr vI, String methodName,
      int lineNumber) {
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
      // System.out.println(lineNumber);
      return true;
    }
    return false;
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
          if (printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
              iStmt.getInvokeExpr(), methodName, tag.getLineNumber())) {
            initialSeedData.add(unit);
          }
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
              if (printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  ((Stmt) unit).getInvokeExpr(), methodName, tag.getLineNumber())) {
                initialSeedData.add(unit);
              }
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
              if (printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  (VirtualInvokeExpr) vB.getValue(), methodName, tag.getLineNumber())) {
                initialSeedData.add(unit);
              }
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
              if (printInvokeStmtLineNumber(src.getDeclaringClass() + "::" + src.getName(),
                  iStmt.getInvokeExpr(), methodName, tag.getLineNumber())) {
                initialSeedData.add(unit);
              }
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
        // Iterator sources = new Sources(cg.edgesInto(target));
        Iterator<Edge> edges = cg.edgesInto(target);

        boolean isStartingPoint = true;
        while (edges.hasNext()) {
          Edge edge = edges.next();
          // edge.srcStmt()

          // StringBuilder srcSig = new StringBuilder("NULL"); // name of srcSig which calls a
          // starting
          // // point which can be JVM,
          // Thrift lib

          // if (sources.hasNext()) {
          // while (sources.hasNext()) {
          SootMethod parent = (SootMethod) edge.getSrc();
          // the value of srcSig might not be correct
          // srcSig.append(parent.getSignature() + "::::");
          if (target.getSignature().contains(externalInvocationMethods)
              && parent.getSignature().contains(externalInvocationMethods)) {
            System.out.println("externalInvocationMethods");
            continue;
          }
          // SootMethod p2 = edge.src();
          // if (parent != p2) {
          // System.out.println("DIFFERENT " + parent + " :::: " + p2);
          // }

          // it seems edge.src() and edge.getSrc() return the same result
          if (!parent.getSubSignature().equals("void <clinit>()")
          // && !target.getSubSignature().contains("void <init>")
          ) {
            if (parent.getSignature().contains(programPrefix)) {
              System.out.println("Push " + node.getId() + ": " + target.getSignature()
                  + " called-by " + parent.getSignature() + " : " + parent.getSubSignature()
                  + " stmt " + edge.srcStmt() + " at "
                  + edge.srcStmt().getJavaSourceStartLineNumber());
              Stmt stmt = edge.srcStmt();


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
            System.out.println("clinit || init");
          }
        }
        // }

        if (isStartingPoint) {
          // if (!target.isEntryMethod()) { //why do I need this
          // srcSig.append(" ENTRY METHOD");
          // }
          if (target.isEntryMethod()) {
            System.out.println("EntryMethod::: " + target.getSignature());
          }
          graph.addStartingNode(target.getSignature(), currentOptionAPI, node.getId());
          // }
          System.out.println("Starting Point " + target.getSignature() + " has ID " + node.getId());

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


  public int analyseCallGraph(CallGraph cg, Set<String> confClassNames, String optionAPI) {

    this.currentOptionAPI = optionAPI;
    countMatch = 0;
    SootMethod target = null;

    boolean isBreak = false;

    // Chain<SootClass> classes = Scene.v().getClasses();
    for (String className : confClassNames) {
      if (Scene.v().containsClass(className)) {
        // for (SootClass sClass : classes) {
        SootClass sClass = Scene.v().getSootClass(className);
        // System.out.println(sClass.getName());
        target = null;

        // if (className.contains(sClass.getName())) {
        try {
          target = sClass.getMethodByName(optionAPI);
        } catch (Exception e) {

        }

        if (target != null) {

          System.out.println("Checking " + target.getSignature());
          // if (graph.containsNode(target.getSignature()) == -1) {
          System.out.println("Findng incoming edge");
          CCGNode node = new CCGNode(target.getSignature()); // we need to reset NEXT_ID
          System.out.println("FIRST NODE " + node.getId());
          // node.addOutNodeId(Integer.MAX_VALUE);
          int graphID = 1;
          Iterator sources = new Sources(cg.edgesInto(target));
          int nbMethodsWhichUseThisOption = 0;
          while (sources.hasNext()) {
            graph = new CCGraph(this.version, graphID);
            graph.addNewNode(node.getId(), node);
            sMStack.clear();
            idStack.clear();
            SootMethod src = (SootMethod) sources.next();
            System.out.println("");
            System.out.println(target + " " + target.getParameterCount()
                + " params might be called by " + src);
            // we need to consider this to avoid exploding graph, for example
            // getConcurrentCounterWriters of org.apache.cassandra.concurrent.StageManager
            // in Cassandra 2.1.8

            if (!src.getSubSignature().equals("void <clinit>()")) {
              try {
                buildPartICoGNoStack(cg, node.getId(), src);
                // System.out.println("NOT HERE");
              } catch (Exception e) {
                e.printStackTrace();
              }
            } else { // if we do not deal with clinit, we need to create a starting node here
              graph.addStartingNode(target.getSignature(), currentOptionAPI, node.getId());
            }

            countMatch++;
            getCallSiteInformation(src, optionAPI);
            // return;
            isBreak = true;
            graphID += graph.princetonDFS();
            // graph.removeCycle();
            // graph.DFS();
          }
          // }
          // else {
          // System.out.println("Analyzed " + target.getSignature());
          // }

          if (isBreak)
            break;
        }
        // }
        if (isBreak)
          break;
      }
      if (isBreak)
        break;
    }
    System.out.println("THERE ARE " + countMatch);
    return countMatch;
  }
}
