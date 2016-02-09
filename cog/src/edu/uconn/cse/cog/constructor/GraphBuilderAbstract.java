package edu.uconn.cse.cog.constructor;

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
import soot.Value;
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

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

public class GraphBuilderAbstract {

  public static int countMatch;
  protected String libFile;
  private String mainClass;
  protected String startingPointFile;
  protected String optionAPIFile;
  protected boolean parseOneOption;
  protected String oAPI;
  private String currentOptionAPI;
  protected String mightRechableMethodFileName;
  protected String rechableMethodFileName;
  protected String reallyRechableMethodFileName;
  protected int detectRechableMethod;
  protected int printAllRealRechableMethod;
  protected String version;
  protected String generalInfoFolder;
  protected FileWriter generalInfoFW;
  protected FileWriter settingNameFW;
  protected FileWriter startingPointFW;
  protected String settingNameFileName;
  public CCGraph graph;
  private int nbVertices = 0;

  protected static String programPrefix;
  // protected static Set<String> externalInvocationMethods = new HashSet<String>();
  protected static String externalInvocationMethods = null;

  Set<Unit> initialSeedData = new HashSet<Unit>();

  private Stack<SootMethod> sMStack = new Stack<SootMethod>();
  private Stack<Integer> idStack = new Stack<Integer>();
  final static int MAX_LEVEL = 0;

  protected void initialize() {
    libFile = "data/cass.txt";
    startingPointFile = "data/cassPIIEntryPoints.txt";
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
      startingPointFile = prop.getProperty("startingPointFile");
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
      generalInfoFolder = prop.getProperty("generalInfoFolder");
      settingNameFileName = prop.getProperty("settingNameFileName");
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("VERSION " + version);
    System.out.println("METHOD " + oAPI);// Util.getMethodName(optionSource));
    System.out.println("mainClass " + mainClass);
    System.out.println("libFile " + libFile);
  }

  private boolean printInvokeStmtLineNumber(String callsiteInfo, InvokeExpr vI, String methodName,
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

  private void getCallSiteInformation(SootMethod src, String methodName) {
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



  private void processExternalLibCall(CallGraph cg, String directParent, SootMethod target,
      int level) {
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



  private void buildCCGNoStack(CallGraph cg, int firstSrcId, SootMethod firstSource) {
    System.out.println("buildCCGNoStack");
    if (!firstSource.getSignature().contains(programPrefix)) {
      return;
    }

    sMStack.push(firstSource);
    idStack.push(firstSrcId);
    while (!sMStack.empty()) {
      SootMethod target = sMStack.pop();
      int srcId = idStack.pop();
      System.out.println("Pop " + srcId + ": " + target.getSignature());

      int targetId = graph.containsNode(target.getSignature());
      if (targetId != -1) {
        graph.updateOutIdForNode(targetId, srcId);
        System.out.println("Visited");
        // return;
      } else {
        CCGNode node = new CCGNode(target.getSignature());
        nbVertices++;
        node.addOutNodeId(srcId);
        graph.addNewNode(node.getId(), node);
        // Iterator sources = new Sources(cg.edgesInto(target));

        Iterator<Edge> edges = cg.edgesInto(target);
        boolean isStartingPoint = true;
        while (edges.hasNext()) {
          Edge edge = edges.next();
          SootMethod parent = (SootMethod) edge.getSrc();
          // the value of srcSig might not be correct
          // srcSig.append(parent.getSignature() + "::::");
          if (target.getSignature().contains(externalInvocationMethods)
              && parent.getSignature().contains(externalInvocationMethods)) {
            System.out.println("externalInvocationMethods");
            continue;
          }
          // it seems edge.src() and edge.getSrc() return the same result
          if (!parent.getSubSignature().equals("void <clinit>()")) {
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
          if (startingPointFW != null) {
            try {
              startingPointFW.write(target.getSignature() + "\n");
            } catch (Exception e) {

            }
          }
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
      // break; //for testing
    }
  }

  Set<String> markedAnalysisPoints = new HashSet<String>();

  protected int analyseCallGraph(CallGraph cg, Set<String> confClassNames, String optionAPI)
      throws IOException {
    this.currentOptionAPI = optionAPI;
    countMatch = 0;
    SootMethod source;

    boolean isBreak = false;

    for (String className : confClassNames) {
      if (Scene.v().containsClass(className)) {
        // for (SootClass sClass : classes) {
        SootClass sClass = Scene.v().getSootClass(className);
        System.out.println("analyseCallGraph " + sClass.getName());
        source = null;

        // for (SootMethod sm : sClass.getMethods()) {
        // if (sm.hasActiveBody())
        // System.out.println(sm.getSignature());
        // }

        // if (className.contains(sClass.getName())) {
        try {
          // example: getInt() in Hadoop
          source = sClass.getMethodByName(optionAPI);
        } catch (Exception e) {
        }

        if (source != null) {
          if (settingNameFW != null) {
            Iterator<Edge> edges = cg.edgesInto(source);
            while (edges.hasNext()) {
              Edge e = edges.next();
              Stmt stmt = e.srcStmt();
              if (stmt.containsInvokeExpr())
                // for (int i = 0; i < stmt.getInvokeExpr().getArgCount(); ++i)
                if (stmt.getInvokeExpr().getArgCount() >= 2)
                  settingNameFW.write(optionAPI + "\t"
                      + stmt.getInvokeExpr().getArg(0).toString().replaceAll("\"", "").trim()
                      + "\n");
            }
          }

          System.out.println("Checking " + source.getSignature());
          // if (graph.containsNode(target.getSignature()) == -1) {
          System.out.println("Findng incoming edge");
          CCGNode node = new CCGNode(source.getSignature()); // we need to reset NEXT_ID
          System.out.println("FIRST NODE ID " + node.getId());
          // node.addOutNodeId(Integer.MAX_VALUE);
          int graphID = 1;

          Iterator sources = new Sources(cg.edgesInto(source));

          int nbMethodsWhichUseThisOption = 0;
          markedAnalysisPoints.clear();
          if (sources.hasNext()) {
            while (sources.hasNext()) {
              graph = new CCGraph(this.version, graphID);
              graph.addNewNode(node.getId(), node);
              sMStack.clear();
              idStack.clear();
              SootMethod src = (SootMethod) sources.next();
              nbVertices = 0;
              if (!markedAnalysisPoints.contains(src.getSignature())) {
                markedAnalysisPoints.add(src.getSignature());
                System.out.println("");
                System.out.println(source + " " + source.getParameterCount()
                    + " params might be called by " + src);
                // we need to consider this to avoid exploding graph, for example
                // getConcurrentCounterWriters of org.apache.cassandra.concurrent.StageManager
                // in Cassandra 2.1.8

                if (!src.getSubSignature().equals("void <clinit>()")) {
                  try {
                    buildCCGNoStack(cg, node.getId(), src);

                    // System.out.println("NOT HERE");
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                } else { // if we do not deal with clinit, we need to create a starting node here
                  graph.addStartingNode(source.getSignature(), currentOptionAPI, node.getId());
                }

                countMatch++;
                getCallSiteInformation(src, optionAPI);
                // return;
                isBreak = true;
                graphID += graph.princetonDFS();
                System.out.println("nbVertices " + nbVertices);
                generalInfoFW.write(src.getSignature() + "\t" + nbVertices + "\n");
                // graph.removeCycle();
                // graph.DFS();
              }
            }
          } else {
            System.out.println("No incoming edge ");
          }

          if (isBreak)
            continue;
        } else {
          System.out.println("Cannot find method " + optionAPI);
        }
        // }
        if (isBreak)
          continue;
      }
      // if (isBreak)
      // break;
    }
    System.out.println("THERE ARE " + countMatch);
    return countMatch;
  }
}
