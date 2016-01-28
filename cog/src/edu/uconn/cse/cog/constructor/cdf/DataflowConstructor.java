package edu.uconn.cse.cog.constructor.cdf;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.Util;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.IInfoflow;
import soot.jimple.infoflow.Infoflow;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataflowConstructor extends GraphBuilderAbstract {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Use the following argument to run the program");
      System.out.println("0 to run the program with Cassandra");
      System.out.println("1 to run the program with Elastic");
      System.out.println("2 to run the program with Hadoop");
      System.out.println("3 to run the program with HBase");
    }

    int option = Integer.parseInt(args[0]);
    switch (option) {
      case 0:
        CassandraDataflowConstructor cassPIICoG = new CassandraDataflowConstructor();
        cassPIICoG.constructDataflow();
        break;
      case 2:
        HadoopDataflowConstructor hadoopPIICoG = new HadoopDataflowConstructor();
        hadoopPIICoG.constructDataflow();
        break;
      default:
        System.out.println("Use the following argument to run the program");
        System.out.println("0 to run the program with Cassandra");
        System.out.println("1 to run the program with Elastic");
        System.out.println("2 to run the program with Hadoop");
        System.out.println("3 to run the program with HBase");
    }
  }
}
