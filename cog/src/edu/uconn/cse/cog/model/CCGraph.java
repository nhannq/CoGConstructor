package edu.uconn.cse.cog.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CCGraph {
  private class StartingNode {
    String optionAPI;
    String target;

    // target: name of callee, src: name of caller
    public StartingNode(String target, String optionAPI) {
      this.target = target;
      this.optionAPI = optionAPI;
    }

    public String toString() {
      // " ::: " + target.split(":")[1].split("\\s+")[1].replaceAll(">", "")
      return optionAPI;
    }
  }

  public CCGraph(String version, int graphID) {
    this.version = version;
    this.graphID = graphID;
  }

  int graphID = 1;
  String version;
  String optionAPI;
  FileWriter fWriter;

  // map method signature : corresponding node's id
  HashMap<StartingNode, Integer> startingNodes = new HashMap<StartingNode, Integer>();
  Set<String> uniqueStartingNodes = new HashSet<String>();
  // map node's id : node
  HashMap<Integer, CCGNode> nodes = new HashMap<Integer, CCGNode>();

  HashMap<String, Integer> nodeToId = new HashMap<String, Integer>();
  Set<Integer> visited = new HashSet<Integer>();
  ArrayList<CCGPath> paths = new ArrayList<CCGPath>();

  public void addStartingNode(String targetSig, String optionAPI, int id) {
    startingNodes.put(new StartingNode(targetSig, optionAPI), id);
    uniqueStartingNodes.add(targetSig);
  }

  public void setOptionAPI(String optionAPI) {
    this.optionAPI = optionAPI;
  }

  public int containsNode(String methodSignature) {
    if (nodeToId.containsKey(methodSignature)) {
      return nodeToId.get(methodSignature);
    } else {
      return -1;
    }
  }

  public void updateOutIdForNode(int nodeId, int outId) {
    CCGNode node = nodes.get(nodeId);
    node.addOutNodeId(outId);
    nodes.put(nodeId, node);
  }

  public void addNewNode(int nodeId, CCGNode node) {
    // System.out.println("NEW " + nodeId + " " + node.toString());
    nodes.put(nodeId, node);
    nodeToId.put(node.getMethodSig(), nodeId);
  }

  public int size() {
    return nodes.size();
  }

  private boolean[] marked; // marked[v] = has vertex v been marked?
  private int[] edgeTo; // edgeTo[v] = previous vertex on path to v
  private boolean[] onStack; // onStack[v] = is vertex on the stack?
  private Stack<Integer> cycle; // directed cycle (or null if no such cycle)
  private int startingNodeId;

  // CCGPath path = new CCGPath();

  public int princetonDFS() {
    System.out.println("princetonDFS");
    System.out.println("startingNodes size " + startingNodes.size());
    HashMap<String, Integer> optionAPIToGraphID = new HashMap<String, Integer>();
    try {
      for (StartingNode sMethod : startingNodes.keySet()) { //
        // HashMap<Integer, CCGNode> tmpNodes = new HashMap<Integer, CCGNode>(nodes);

        optionAPI = sMethod.toString();
        if (optionAPIToGraphID.containsKey(optionAPI)) {
          graphID = optionAPIToGraphID.get(optionAPI) + 1;
        } else {
          File file = new File("ccg" + version + "/" + optionAPI + "/");
          if (!file.exists()) {
            if (file.mkdir()) {
            } else {
              System.out.println("Failed to create directory!");
            }
          }
        }
        optionAPIToGraphID.put(optionAPI, graphID);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element graphElement = doc.createElement("graph");
        doc.appendChild(graphElement);

        fWriter = new FileWriter("ccg" + version + "/" + optionAPI + "/" + graphID + "-2");

        // new FileOutputStream(version + "-result/" + optionAPI + "-" + graphID + ".xml");
        startingNodeId = startingNodes.get(sMethod);
        marked = new boolean[nodes.get(startingNodeId).getMaxID() + 1]; // temporarily use this, we
                                                                        // can optimize later
        onStack = new boolean[nodes.get(startingNodeId).getMaxID() + 1];
        edgeTo = new int[nodes.get(startingNodeId).getMaxID() + 1];
        cycle = null;
        Element rootXML = doc.createElement("root");
        graphElement.appendChild(rootXML);
        paths.clear();
        // path.clear();

        fWriter.write("ROOT IS " + optionAPI + "\n");
        System.out.println("ROOT " + optionAPI + " has ID " + startingNodeId);

        fWriter.write("Calling performsPrincetonDFS\n");
        fWriter.write("Starting Node " + startingNodes.get(sMethod) + " has "
            + nodes.get(startingNodes.get(sMethod)).outNodeIds.size() + " children\n");
        performsPrincetonDFS(-1, startingNodes.get(sMethod), doc, rootXML);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result =
            new StreamResult(new File("ccg" + version + "/" + optionAPI + "/" + graphID + ".xml"));
        System.out.println("Writing Result to " + "ccg" + version + "/" + optionAPI + "/" + graphID
            + ".xml");

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
        fWriter.close();

        dumpPaths();
        // nodes = tmpNodes;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return startingNodes.size();
  }

  private void performsPrincetonDFS(int prevNodeId, int nodeId, Document doc, Element rootXML) {
    try {
      fWriter.write("performsPrincetonDFS at " + nodeId);
      onStack[nodeId] = true;
      marked[nodeId] = true;
      CCGNode node = nodes.get(nodeId);
      // path.addNode(node);
      fWriter.write("Visiting " + prevNodeId + "-" + nodeId + " " + node.toString() + "\n");

      boolean isLeaf = false;
      if (node.hasNoOutNode()) {
        fWriter.write("--------\n");
        isLeaf = true;
        // return;
      } else {
        Set<Integer> outNodeIds = node.getOutNodeIds();
        // System.out.println("outNodeIds Size " + outNodeIds.size());

        for (Integer outNodeId : outNodeIds) {
          // nodes.get(nodeId).nvVisitedDescendants++;
          // System.out.println("Child " + outNodeId);
          // if (cycle != null)
          // return;
          // else
          if (marked[outNodeId]) {
            fWriter.write("Did visit " + nodeId + "-" + outNodeId + "\n");
          }
          if (!marked[outNodeId]) {
            edgeTo[outNodeId] = nodeId;
            isLeaf = false;
            performsPrincetonDFS(nodeId, outNodeId, doc, rootXML);
          }
          // trace back directed cycle
          else if (onStack[outNodeId]) {
            fWriter.write("There is a cycle\n");
            cycle = new Stack<Integer>();
            for (int x = nodeId; x != outNodeId; x = edgeTo[x]) {
              // nodes.get(x).outNodeIds.remove(edgeTo[x]);
              cycle.push(x);
            }
            cycle.push(outNodeId);
            HashSet<Integer> removedOutNodeId;
            if (needToBeRemovedEdge.containsKey(nodeId)) {
              removedOutNodeId = needToBeRemovedEdge.get(nodeId);
            } else {
              removedOutNodeId = new HashSet<Integer>();
            }
            removedOutNodeId.add(outNodeId);
            needToBeRemovedEdge.put(nodeId, removedOutNodeId);
            fWriter.write("Will remove " + nodeId + "-" + outNodeId + "\n");

            // cycle.push(nodeId);
            System.out.println("CallingCheck");
            check();
          }
        }
      }

      if (isLeaf) {
        CCGPath path = new CCGPath();
        for (int x = nodeId; x != startingNodeId; x = edgeTo[x]) {
          // nodes.get(x).outNodeIds.remove(edgeTo[x]);
          path.addNode(nodes.get(x));
        }
        path.addNode(nodes.get(startingNodeId));
        paths.add(path);
      }


      // path.remove(node);
      onStack[nodeId] = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // boolean visitedAllDescentdants(int nodeId) {
  // for (Integer outNodeId : nodes.get(nodeId).outNodeIds) {
  // if (!nodes.get(outNodeId).visited) {
  // return false;
  // }
  // }
  // return true;
  // }

  HashMap<Integer, HashSet<Integer>> needToBeRemovedEdge = new HashMap<Integer, HashSet<Integer>>();

  public void removeCycle() {
    for (Integer nodeId : needToBeRemovedEdge.keySet()) {
      for (Integer outNodeId : needToBeRemovedEdge.get(nodeId)) {
        try {
          fWriter.write("Removing " + nodeId + "-" + outNodeId + "\n");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        nodes.get(nodeId).outNodeIds.remove(outNodeId);
      }
    }
  }

  public void dumpPaths() {
    try {
      FileWriter fWriter = new FileWriter("ccg" + version + "/" + optionAPI + "/" + graphID + "-p");
      for (CCGPath path : paths) {
        fWriter.write(path.printPath());
      }
      fWriter.close();
    } catch (Exception e) {

    }

  }

  public boolean hasCycle() {
    return cycle != null;
  }

  public Iterable<Integer> cycle() {
    return cycle;
  }

  int countCycle = 0;

  private boolean check() {
    if (hasCycle()) {
      System.out.println("OutputCycle " + countCycle++);
      // verify cycle
      int first = -1, last = -1;
      for (int v : cycle()) {
        if (first == -1)
          first = v;
        last = v;
      }
      if (first != last) {
        try {
          fWriter.write("cycle begins with " + last + " and ends with " + first + " \n");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return false;
      }
    }
    return true;
  }


  public void printStartingNodes() {
    System.out.println("\n=================**=================");
    System.out.println("UNIQUE STARTING NODES " + uniqueStartingNodes.size());
    System.out.println("STARTING NODES: " + startingNodes.size());
    for (StartingNode s : startingNodes.keySet()) {
      System.out.println(s);
    }
    System.out.println("=================**=================\n");
  }

}
