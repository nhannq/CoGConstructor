package edu.uconn.cse.cog.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetOptionNames {
  String getMethodName(String methodSig) {
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

  String getType(String optionStmt) {
    String result = "";
    try {
      result = optionStmt.split(":")[1].trim().split("\\s+")[0];
    } catch (Exception e) {

    }
    return result;
  }

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

  void readOption(String folderName, Map<String, String> optionNames) {
    File folder = new File(folderName);
    File[] listOfFiles = folder.listFiles();
    // String fileName = "getInt-source";
    List<String> optionStatements;
    for (File f : listOfFiles) {
      if (f.isFile() && f.getName().contains("-source")) {
        optionStatements = new ArrayList<String>();
        readFile(folderName + "/" + f.getName(), optionStatements);
        for (String optionStmt : optionStatements) {
          if (optionStmt.contains("\"")) {
            // System.out.println(optionStmt.split("\"")[1].trim());
            optionNames.put(optionStmt.split("\"")[1].trim(), getType(optionStmt));
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    Properties prop = new Properties();
    String folderName1 = "";
    String folderName2 = "";
    String folderName3 = "";
    Map<String, Integer> typeCounts = new HashMap<String, Integer>();
    GetOptionNames gONames = new GetOptionNames();
    try {
      InputStream input = new FileInputStream("analysis.properties");
      // load a properties file
      prop.load(input);
      folderName1 = prop.getProperty("folderName1");
      folderName2 = prop.getProperty("folderName2");
      folderName3 = prop.getProperty("folderName3");
    } catch (Exception e) {
    }

    Map<String, String> optionNames1 = new HashMap<String, String>();
    gONames.readOption(folderName1, optionNames1);
    Map<String, String> optionNames2 = new HashMap<String, String>();
    gONames.readOption(folderName2, optionNames2);
    Map<String, String> optionNames3 = new HashMap<String, String>();
    gONames.readOption(folderName3, optionNames3);

    int removed1 = 0;
    int removed2 = 0;
    int add2 = 0;
    int add3 = 0;
    for (String s : optionNames1.keySet()) {
      if (!optionNames2.keySet().contains(s)) {
        removed1++;
      }
    }

    for (String s : optionNames2.keySet()) {
      if (!optionNames1.keySet().contains(s)) {
        System.out.println(s);
        add2++;
      }
    }

    for (String s : optionNames2.keySet()) {
      if (!optionNames3.keySet().contains(s)) {
        removed2++;
      }
    }

    for (String s : optionNames3.keySet()) {
      if (!optionNames2.keySet().contains(s)) {
        System.out.println(s);
        add3++;
      }
    }

    int changeType2 = 0;
    for (String s : optionNames1.keySet()) {
      if (optionNames2.containsKey(s)) {
        if (!optionNames1.get(s).equals(optionNames2.get(s))) {
          System.out.println(s + " : " + optionNames1.get(s) + " : " + optionNames2.get(s));
          changeType2++;
        }
      }
    }

    int changeType3 = 0;
    for (String s : optionNames2.keySet()) {
      if (optionNames3.containsKey(s)) {
        if (!optionNames2.get(s).equals(optionNames3.get(s))) {
          changeType3++;
        }
      }
    }

    int changeType1 = 0;
    for (String s : optionNames1.keySet()) {
      if (optionNames3.containsKey(s)) {
        if (!optionNames3.get(s).equals(optionNames2.get(s))) {
          changeType1++;
        }
      }
    }

    for (String s : optionNames3.keySet()) {
      if (typeCounts.containsKey(optionNames3.get(s))) {
        typeCounts.put(optionNames3.get(s), typeCounts.get(optionNames3.get(s)) + 1);
      } else {
        typeCounts.put(optionNames3.get(s), 1);
      }
    }
    // for (String optName : optionNames1.keySet()) {
    // System.out.println(optName + " type " + optionNames1.get(optName));
    // }
    // System.out.println(optionNames1.size());
    System.out.println("REMOVED FROM 1 " + removed1);
    System.out.println("REMOVED FROM 2 " + removed2);
    System.out.println("ADD TO 2 " + add2);
    System.out.println("ADD TO 3 " + add3);
    System.out.println("CHANGE TYPE V1 - V2 " + changeType2);
    System.out.println("CHANGE TYPE V2 - V3 " + changeType3);
    System.out.println("CHANGE TYPE V1 - V3 " + changeType1);
    System.out.println("NB OPTIONS " + optionNames3.size());

    for (String s : typeCounts.keySet()) {
      System.out.println(s + " : " + typeCounts.get(s));
    }
  }
}
