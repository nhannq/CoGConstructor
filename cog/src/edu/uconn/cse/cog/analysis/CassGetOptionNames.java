package edu.uconn.cse.cog.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CassGetOptionNames {
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

  static void getRealOptionName(Set<String> unprocessNames, Set<String> realNames) {
    for (String s : unprocessNames) {
      realNames.add(s.trim().split(":")[0].trim());
    }
  }

  static void analyseCassandraOptionChanges() {
    String fileVersion1 = "/home/nnguyen/Setup/eclipse/ccgdata/cassOption1219.txt";
    String fileVersion2 = "/home/nnguyen/Setup/eclipse/ccgdata/cassOption207.txt";
    String fileVersion3 = "/home/nnguyen/Setup/eclipse/ccgdata/cassOption218.txt";
    Set<String> tmpoptionsVersion1 = new HashSet<String>();
    Set<String> tmpoptionsVersion2 = new HashSet<String>();
    Set<String> tmpoptionsVersion3 = new HashSet<String>();
    readFile(fileVersion1, tmpoptionsVersion1);
    readFile(fileVersion2, tmpoptionsVersion2);
    readFile(fileVersion3, tmpoptionsVersion3);
    Set<String> optionsVersion1 = new HashSet<String>();
    Set<String> optionsVersion2 = new HashSet<String>();
    Set<String> optionsVersion3 = new HashSet<String>();
    getRealOptionName(tmpoptionsVersion1, optionsVersion1);
    getRealOptionName(tmpoptionsVersion2, optionsVersion2);
    getRealOptionName(tmpoptionsVersion3, optionsVersion3);
    int nbAddedOptions2 = 0;
    int nbRemovedOptions1 = 0;
    int nbAddedOptions3 = 0;
    int nbRemovedOptions2 = 0;

    for (String s : optionsVersion1) {
      if (!optionsVersion2.contains(s)) {
        nbRemovedOptions1++;
        // System.out.println(s);
      }
    }

    for (String s : optionsVersion2) {
      if (!optionsVersion3.contains(s)) {
        nbRemovedOptions2++;
        // System.out.println(s);
      }
    }

    System.out.println("==========");
    for (String s : optionsVersion2) {
      if (!optionsVersion1.contains(s)) {
        nbAddedOptions2++;
        // System.out.println(s);
      }
    }

    for (String s : optionsVersion3) {
      if (!optionsVersion2.contains(s)) {
        nbAddedOptions3++;
        // System.out.println(s);
      }
    }
    System.out.println("NB REMOVED FROM 1 " + nbRemovedOptions1);
    System.out.println("NB REMOVED FROM 2 " + nbRemovedOptions2);
    System.out.println("NB ADDED OPTIONS TO 2 " + nbAddedOptions2);
    System.out.println("NB ADDED OPTIONS TO 3 " + nbAddedOptions3);
  }

  public static void main(String[] args) {

    CassGetOptionNames gONames = new CassGetOptionNames();
    analyseCassandraOptionChanges();

    Map<String, Integer> typeCounts = new HashMap<String, Integer>();
    List<String> configAPIs = new ArrayList<String>();
    readFile("data/cass218OptionAPIS.txt", configAPIs);

    for (String confAPI : configAPIs) {
      String type = confAPI.split(":")[1].trim().split("\\s+")[0];

      if (typeCounts.containsKey(type)) {
        typeCounts.put(type, typeCounts.get(type) + 1);
      } else {
        typeCounts.put(type, 1);
      }
    }

    for (String s : typeCounts.keySet()) {
      System.out.println(s + " : " + typeCounts.get(s));
    }
  }
}
