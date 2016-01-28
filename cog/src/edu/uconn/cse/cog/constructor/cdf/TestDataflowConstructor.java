package edu.uconn.cse.cog.constructor.cdf;

import edu.uconn.cse.cog.constructor.GraphBuilderAbstract;
import edu.uconn.cse.cog.util.Util;

import soot.Scene;
import soot.SootMethod;

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

public class TestDataflowConstructor extends GraphBuilderAbstract {
  public static void main(String[] args) {
    TestDataflowConstructor cPIICoG = new TestDataflowConstructor();
    cPIICoG.constructDataflow();
  }

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

  public void constructDataflow() {
    String appPath = "/home/nnguyen/workspaceluna/sootinfoapptest/sootinfoapptest.jar";
    String libFile = "data/apptest.txt";
    String entryPointFile = "data/apptestPIIEntryPoints.txt";
    String startingPointFile = "data/apptestStartingPoint.txt";
    boolean parseOneOption = false;
    String optionSource = "<edu.uconn.cse.appinfo.test.Main: java.lang.String source()>";
    String optionSink = "";
    String version = "1.0.0";
    String programPrefix = "";
    boolean firstRun = false;
    String sourceFolder = "";

    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream("config.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      appPath = prop.getProperty("appPath");
      libFile = prop.getProperty("libFile");
      entryPointFile = prop.getProperty("entryPointFile");
      startingPointFile = prop.getProperty("startingPointFile");
      if (Integer.parseInt(prop.getProperty("parseOneOption")) == 1) {
        parseOneOption = true;
        optionSource = prop.getProperty("optionSource");
      }
      if (Integer.parseInt(prop.getProperty("firstRun")) == 1) {
        firstRun = true;
      }
      version = prop.getProperty("version");
      programPrefix = prop.getProperty("programPrefix");
      optionSink = prop.getProperty("optionSink");
      sourceFolder = prop.getProperty("sourceFolder");
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("VERSION " + version);
    System.out.println("METHOD " + getMethodName(optionSource));
    System.out.println("appPath " + appPath);
    System.out.println("libFile " + libFile);



    // String appPath =
    // "/home/nqn12001/workspaceluna/apache-cassandra-2.1.8-src/build/classes/main/";
    List<String> argsList = new ArrayList<String>();
    Util.readFile(libFile, argsList);
    String libPath = argsList.get(1);
    System.out.println(libPath);

    // <soot.jimple.infoflow.test.TestNoMain: java.lang.String function1()>
    // List<String> entryPointList = new ArrayList<String>() {
    // {
    // add("<org.apache.cassandra.service.CassandraDaemon: void main(java.lang.String[])>");
    // }
    // };

    List<String> entryPointList = new ArrayList<String>();
    Util.readFile(entryPointFile, entryPointList);



    HashSet<String> tmpRawSources = new HashSet<String>();
    HashSet<String> rawSources = new HashSet<String>();
    Util.readFile(startingPointFile, tmpRawSources);
    if (!parseOneOption) {
      for (String source : tmpRawSources) { // need to fix this
        rawSources.add(source.substring(source.indexOf("<"), source.lastIndexOf(">") + 1));
      }
    }

    List<String> wholeSources;
    if (!parseOneOption) {
      wholeSources = new ArrayList<String>(rawSources);
      // System.out.println(sources.size());
      // for (String s : sources) {
      // // System.out.println(s);
      // }
    } else {
      wholeSources = new ArrayList<String>();

      // add("<test.Main: void main(java.lang.String[])>");
      // add("<org.apache.cassandra.config.DatabaseDescriptor: long getWriteRpcTimeout()>");
      // add("<org.apache.cassandra.config.DatabaseDescriptor: int getSSLStoragePort()>");
      // add("<org.apache.cassandra.config.DatabaseDescriptor: boolean hasCrossNodeTimeout()>");
      wholeSources.add(optionSource);
    }

    List<String> sinks = new ArrayList<String>() {
      {
        // add("<test.Main: int foo2()>");
        // add("<test.Main: void sink(java.lang.String)>");
        // add("<test.Main: void sink2(java.lang.String)>");
        add("<java.io.PrintStream: void println(java.lang.String)>");
        add("<java.io.PrintStream: void println(int)>");
        // add("<test.Main: int secret()>");
      }
    };

    FileWriter infoWriter = null;
    try {
      infoWriter = new FileWriter(version + "-result/" + "information.txt");
      for (String source : wholeSources) {
        System.out.println("PARSING " + source);

        // TODO: temporarily disabled
        // IInfoflow infoflow =
        // new NhanInfoflow(version, getMethodName(source), programPrefix, firstRun, sourceFolder,
        // infoWriter);
        // List<String> sources = new ArrayList<String>();
        // sources.add(source);
        // infoflow.computeInfoflow(appPath, libPath, entryPointList, sources, sinks);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (infoWriter != null) {
        try {
          infoWriter.close();
        } catch (IOException e) {

        }
      }
    }
    List<SootMethod> entryPoints = Scene.v().getEntryPoints();
    for (SootMethod sMethod : entryPoints) {
      System.out.println("EntryPoint " + sMethod.getName() + " " + sMethod.getSignature() + " of "
          + sMethod.getDeclaringClass().getName());
      // Iterator sources = new Sources(cg.edgesInto(sMethod));

      // while (sources.hasNext()) {
      // SootMethod src = (SootMethod) sources.next();
      // if (src.getDeclaringClass().getName().contains("org.apache"))
      // System.out.println(sMethod + " might be called by " + src);
      // }
      // break;
    }

  }
}
