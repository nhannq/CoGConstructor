package edu.uconn.cse.cog.model;

import java.util.HashSet;
import java.util.Set;

public class CCGNode {
  
  public enum Color {
    WHITE, GREY, BLACK;
  }
  static protected int NEXT_ID;
  private int id;
  private String methodSignature;
  Set<Integer> outNodeIds = new HashSet<Integer>(); // list of all methods called by this method
//  public boolean visitedAllDescentdants = false;
//  public int nvVisitedDescendants = 0;
//  public boolean visited = false;
//  public Color mark;
  public Set<Integer> visitedOutNodesIds = new HashSet<Integer>();

  public CCGNode(String methodSignature) {
    this.id = NEXT_ID;
    NEXT_ID++;
    this.methodSignature = methodSignature;
  }
  
  public CCGNode(String methodSignature, int resetNextID) {
    NEXT_ID = resetNextID;
    this.id = NEXT_ID;
    NEXT_ID++;
    this.methodSignature = methodSignature;
  }

  public int getId() {
    return this.id;
  }

  public void addOutNodeId(int outId) {
    // System.out.println(this.id + " - " + outId);
    outNodeIds.add(outId);
  }

  public boolean hasNoOutNode() {
    return outNodeIds.size() == 0;
  }

  Set<Integer> getOutNodeIds() {
    return outNodeIds;
  }

  public String getMethodSig() {
    return this.methodSignature;
  }

  public String toString() {
    return methodSignature;
  }
  
  public int getNbOutNodes() {
    return outNodeIds.size();
  }
  
  public int getMaxID() {
    return NEXT_ID;
  }
  
}
