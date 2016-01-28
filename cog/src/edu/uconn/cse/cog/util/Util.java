package edu.uconn.cse.cog.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
  public static void readFile(String fileName, Collection<String> data) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      try {
        String line = br.readLine();
        while (line != null) {
          data.add(line.trim());
          // System.out.println("LINE " + line);
          line = br.readLine();
        }
      } finally {
        br.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static String readFile(String fileName) {
    try {
      BufferedReader br = new BufferedReader(new FileReader(fileName));
      try {
        String line = br.readLine();
        return line;
      } finally {
        br.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static String getMethodName(String methodSig) {
    Pattern pattern = Pattern.compile(" (.*?)\\(");
    Matcher matcher = pattern.matcher(methodSig);
    String methodName = null;
    if (matcher.find()) {
      methodName = matcher.group(1);
      methodName = methodName.split("\\s+")[1];
    }

    if (methodName != null) {
      System.out.println(methodName);
    }
    return methodName;
  }
}
