package edu.uconn.cse.cog.model;

import java.util.ArrayList;

public class CCGPath {
  ArrayList<CCGNode> path = new ArrayList<CCGNode>();
  
  public void addNode(CCGNode node) {
    path.add(node);
  }
  
  public void remove(CCGNode node) {
    path.remove(node);
  }
  public void clear() {
    path.clear();
  }
  public String printPath() {
    StringBuilder result = new StringBuilder();
    result.append("-----------\n");
    
    for (int i = path.size()-1; i >=0; i--) {
      CCGNode node = path.get(i);
      result.append(node.getId() + " " + node.getMethodSig() + "\n");
    }
    result.append("=========");
    return result.toString();
  }
  
  
}
