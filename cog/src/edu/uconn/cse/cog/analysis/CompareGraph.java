package edu.uconn.cse.cog.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uconn.cse.cog.util.Util;


public class CompareGraph {

  // static final String ROOT = "root";
  // static final String NODE = "node";
  // static final String INMETHOD = "inmethod";
  // static final String INCLASS = "inclass";
  class GraphNode {

    String node;
    String inMethod;
    String inClass;
    boolean isLeaf;
    int prevNodeId;

    public GraphNode(String node, String inMethod, String inClass, int prevNodeId, boolean isLeaf) {
      this.node = node;
      this.inMethod = inMethod;
      this.inClass = inClass;
      this.prevNodeId = prevNodeId;
      this.isLeaf = isLeaf;
    }

    public int compareTo(GraphNode o) {
      if (o.node.equals(this.node) && o.inMethod.equals(this.inMethod)
          && o.inClass.equals(this.inClass) && o.isLeaf == this.isLeaf) {
        return 0;
      }
      return 1;
    }

    public boolean equals(GraphNode o) {
      if (o.node.equals(this.node) && o.inMethod.equals(this.inMethod)
          && o.inClass.equals(this.inClass) && o.isLeaf == this.isLeaf) {
        return true;
      }
      return false;
    }

    public String toString() {
      return node + " ::: " + inMethod + " ::: " + inClass;
    }
  }

  class Path {
    private GraphNode root;
    private List<GraphNode> leaves;
    private Map<Integer, GraphNode> nodes;

    public Path() {
      leaves = new ArrayList<GraphNode>();
      nodes = new HashMap<Integer, GraphNode>();
    }

    public GraphNode getRoot() {
      return this.root;
    }

    public void addRoot(GraphNode root) {
      this.root = root;
    }

    public void addLeaf(GraphNode leaf) {
      leaves.add(leaf);
    }

    public void addNode(int id, GraphNode node) {
      nodes.put(id, node);
    }


    public List<GraphNode> getLeaves() {
      return this.leaves;
    }

    public GraphNode getLeaf(int leafId) {
      if (leafId < leaves.size()) {
        return leaves.get(leafId);
      }
      return null;
    }

    public GraphNode getNode(int id) {
      return nodes.get(id);
    }

    public int getNbLeaves() {
      return leaves.size();
    }

    public boolean equals(Path path) {
      boolean isEqual = true;
      if (this.getNbLeaves() == path.getNbLeaves()) {
        for (int i = 0; i < this.getNbLeaves(); i++) {
          if (!this.getLeaf(i).equals(path.getLeaf(i))) {
            isEqual = false;
            break;
          }
        }
      }
      return isEqual;
    }

    public String toString() {
      String leafInfo = "nil";

      if (leaves.size() > 0)
        leafInfo = leaves.get(0).toString();

      return root.toString() + " : " + leafInfo + " HAS " + leaves.size() + " LEAVES";
    }
  }

  class Graph {
    private List<Path> paths;

    public Graph() {
      paths = new ArrayList<Path>();
    }

    public void addPath(Path path) {
      paths.add(path);
    }

    public Path getPath(int id) {
      if (id < paths.size()) {
        return paths.get(id);
      }
      return null;
    }

    public int getNbPaths() {
      return paths.size();
    }

    public void printInformation() {
      System.out.println("There are " + paths.size() + " paths");
    }

    public void printPaths() {
      if (paths.size() > 0)
        for (Path p : paths) {
          System.out.println(p);
        }
    }
  }

  void readXML(String fileName, Graph graph) {
    try {
      File fXmlFile = new File(fileName);
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(fXmlFile);

      // optional, but recommended
      // read this -
      // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
      doc.getDocumentElement().normalize();

      // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

      NodeList rootList = doc.getElementsByTagName("root");

      // System.out.println("----------------------------");

      for (int rId = 0; rId < rootList.getLength(); rId++) {
        Path path = new Path();
        Node root = rootList.item(rId);
        // System.out.println("\nCurrent Element : " + root.getNodeName() + " "
        // + ((Element) root).getAttribute("id"));

        NodeList nodeList = root.getChildNodes();

        for (int nId = 0; nId < nodeList.getLength(); nId++) {
          Node xmlNode = nodeList.item(nId);
          if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) xmlNode;
            // System.out.println("Node id : " + eElement.getAttribute("id"));
            // System.out.println("NodeStmt : "
            // + eElement.getElementsByTagName("nodeStmt").item(0).getTextContent());
            // System.out.println("in method : "
            // + eElement.getElementsByTagName("inmethod").item(0).getTextContent());
            // System.out.println("in class : "
            // + eElement.getElementsByTagName("inclass").item(0).getTextContent());
            // System.out.println("is leaf : "
            // + eElement.getElementsByTagName("isLeaf").item(0).getTextContent());
            // System.out.println("Prev Node Id : "
            // + eElement.getElementsByTagName("prevNodeId").item(0).getTextContent());
            boolean isLeaf =
                eElement.getElementsByTagName("isLeaf").item(0).getTextContent().equals("YES") ? true
                    : false;
            GraphNode gNode =
                new GraphNode(eElement.getElementsByTagName("nodeStmt").item(0).getTextContent(),
                    eElement.getElementsByTagName("inmethod").item(0).getTextContent(), eElement
                        .getElementsByTagName("inclass").item(0).getTextContent(),
                    Integer.parseInt(eElement.getElementsByTagName("prevNodeId").item(0)
                        .getTextContent()), isLeaf);
            path.addNode(Integer.parseInt(eElement.getAttribute("id")), gNode);
            if (nId == 0)
              path.addRoot(gNode);
            if (isLeaf)
              path.addLeaf(gNode);
          }
        }
        graph.addPath(path);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void findTheDifferentPathImpl(Path smallerPath, Path biggerPath,
      HashMap<Integer, Boolean> hasEqualLeafSmallerPath,
      HashMap<Integer, Boolean> hasEqualComparedLeafBiggerPath) {
    for (int i = 0; i < smallerPath.getNbLeaves(); i++) {
      if (!hasEqualLeafSmallerPath.containsKey(i))
        hasEqualLeafSmallerPath.put(i, false);
      for (int j = 0; j < biggerPath.getNbLeaves(); j++) {
        if (!hasEqualComparedLeafBiggerPath.containsKey(j))
          hasEqualComparedLeafBiggerPath.put(j, false);
        if (biggerPath.getLeaf(j).equals(smallerPath.getLeaf(i))) {
          hasEqualLeafSmallerPath.put(i, true);
          hasEqualComparedLeafBiggerPath.put(j, true);
        }
      }
    }
  }

  void findTheDifferentPath(Path path, Path comparedPath) {
    HashMap<Integer, Boolean> hasEqualLeafSmallerPath = new HashMap<Integer, Boolean>();
    HashMap<Integer, Boolean> hasEqualComparedLeafBiggerPath = new HashMap<Integer, Boolean>();
    if (path.getNbLeaves() < comparedPath.getNbLeaves()) {
      findTheDifferentPathImpl(path, comparedPath, hasEqualLeafSmallerPath,
          hasEqualComparedLeafBiggerPath);
      for (Integer i : hasEqualComparedLeafBiggerPath.keySet()) {
        if (!hasEqualComparedLeafBiggerPath.get(i)) {
          System.out.println(comparedPath.getLeaf(i));
        }
      }
    } else {
      findTheDifferentPathImpl(comparedPath, path, hasEqualLeafSmallerPath,
          hasEqualComparedLeafBiggerPath);
      for (Integer i : hasEqualComparedLeafBiggerPath.keySet()) {
        if (!hasEqualComparedLeafBiggerPath.get(i)) {
          System.out.println(path.getLeaf(i));
        }
      }
    }
  }

  boolean compareGraph(Graph graph, Graph comparedGraph) {
    // System.out.println(graph.getRoot());
    // System.out.println(comparedGraph.getRoot());
    // System.out.println("============");
    // if (graph.getRoot().equals(comparedGraph.getRoot())) {
    // return true;
    // }
    // System.out.println("There are " + graph.getNbPaths() + " paths");
    // System.out.println("There are " + comparedGraph.getNbPaths() + " paths");
    int nbEqualPaths = 0;
    HashMap<Integer, Boolean> hasEqualPathForGraph = new HashMap<Integer, Boolean>();
    HashMap<Integer, Boolean> hasEqualPathForComparedGraph = new HashMap<Integer, Boolean>();
    for (int i = 0; i < graph.getNbPaths(); i++) {
      if (!hasEqualPathForGraph.containsKey(i)) {
        hasEqualPathForGraph.put(i, false);
      }
      Path path = graph.getPath(i);
      System.out.println("path " + path);
      for (int j = 0; j < comparedGraph.getNbPaths(); j++) {
        System.out.println("Comparing " + i + " - " + j);
        if (!hasEqualPathForComparedGraph.containsKey(j)) {
          hasEqualPathForComparedGraph.put(j, false);
        }
        Path comparedPath = comparedGraph.getPath(j);
        System.out.println("ComparedPath " + comparedPath);
        if (path.equals(comparedPath)) {
          nbEqualPaths++;
          hasEqualPathForGraph.put(i, true);
          hasEqualPathForComparedGraph.put(j, true);
          System.out.println("2 paths equal " + i + " and " + j);
        } else {
          if (path.getRoot().equals(comparedPath.getRoot())) {
            System.out.println("2 roots equal");
            findTheDifferentPath(path, comparedPath);
          }
        }
      }
    }
    // if (nbEqualPaths == graph.getNbPaths()) {
    // return true;
    // }
    if (doAllPathsHaveEqualPaths(hasEqualPathForGraph, hasEqualPathForGraph.size()) // graph.getNbPaths())
        && doAllPathsHaveEqualPaths(hasEqualPathForComparedGraph,
            hasEqualPathForComparedGraph.size())) {// comparedGraph.getNbPaths())) {
      return true;
    } else {
      return false;
    }
  }

  boolean doAllPathsHaveEqualPaths(HashMap<Integer, Boolean> map, int mapSize) {
    for (int i = 0; i < mapSize; i++) {
      if (!map.get(i)) {
        return false;
      }
    }
    return true;
  }

  void compare() {
    Properties prop = new Properties();
    InputStream input = null;

    String version = "2.0.7";
    String comparedVersion = "2.1.8";
    String resultFolder = "/home/nnguyen/workspaceluna/CoGConstructor/";

    try {
      input = new FileInputStream("analysis.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      version = prop.getProperty("version");
      comparedVersion = prop.getProperty("comparedVersion");
      resultFolder = prop.getProperty("resultFolder");

    } catch (IOException e) {
      e.printStackTrace();
    }

    List<String> resultList = new ArrayList<String>();
    HashMap<String, Integer> resultInformation = new HashMap<String, Integer>();
    HashMap<String, Integer> comparedResultInformation = new HashMap<String, Integer>();

    Util.readFile(resultFolder + version + "-result/information.txt", resultList);
    for (String s : resultList) {
      resultInformation.put(s.split(":")[0].trim(), Integer.parseInt(s.split(":")[1].trim()));
    }
    resultList.clear();
    Util.readFile(resultFolder + comparedVersion + "-result/information.txt", resultList);
    for (String s : resultList) {
      comparedResultInformation.put(s.split(":")[0].trim(),
          Integer.parseInt(s.split(":")[1].trim()));
    }

    int nbChangedOptions = 0;
    int nbUnchangedOptions = 0;
    for (String s : resultInformation.keySet()) {
      if (comparedResultInformation.containsKey(s)) {
        int nbGraphs = resultInformation.get(s); // number of XML files
        int nbComparedGraphs = comparedResultInformation.get(s);
        int nbEqualGraphs = 0;
        Graph graph = new Graph();
        for (int i = 0; i < nbGraphs; i++) {

          readXML(resultFolder + version + "-result/" + s + "-" + i + ".xml", graph);
          // System.out.println(version + "/" + s + "-" + i + ".xml");
          // graph.printInformation();
        }
        graph.printInformation();
        graph.printPaths();
        Graph comparedGraph = new Graph();
        for (int j = 0; j < nbComparedGraphs; j++) {
          readXML(resultFolder + comparedVersion + "-result/" + s + "-" + j + ".xml", comparedGraph);
          // System.out.println(comparedVersion + "/" + s + "-" + j + ".xml");
          // comparedGraph.printInformation();
        }
        comparedGraph.printInformation();
        comparedGraph.printPaths();
        if (compareGraph(graph, comparedGraph)) {
          // System.out.println("==Equal==");
          // hasEqualGraphForGraph.put(i, true);
          // hasEqualGraphForComparedGraph.put(j, true);
          // nbEqualGraphs++;
          System.out.println("Nothing changed");
          nbUnchangedOptions++;
        } else {
          // System.out.println("==Not Equal==");
          System.out.println("Something changed");
          nbChangedOptions++;
        }
        System.out.println("================");


        // System.out.println("nbEqualGraphs " + nbEqualGraphs);
        // if (nbGraph == nbComparedGraph && nbEqualGraphs >= nbGraph) {
      }
    }
    System.out.println("nbUnchangedOptions: " + nbUnchangedOptions);
    System.out.println("nbChangedOptions: " + nbChangedOptions);
  }

  public static void main(String[] args) {
    CompareGraph cG = new CompareGraph();
    cG.compare();
  }
}
