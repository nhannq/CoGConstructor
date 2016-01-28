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

public class CassandraDataflowConstructor extends GraphBuilderAbstract {

  public void constructDataflow() {
    String appPath =
        "/home/nqn12001/workspaceluna/bin/apache-cassandra-2.0.7/lib/apache-cassandra-2.0.7.jar";
    String libFile = "data/cass.txt";
    String entryPointFile = "data/cass207PIIEntryPoints.txt";
    String settingLoadingMethodFile = "data/cass207LoadingMethods.txt";
    boolean parseOneOption = false;
    String optionSource = "<org.apache.cassandra.config.DatabaseDescriptor: long getRpcTimeout()>";
    String optionSink = "";
    String version = "1.0.0";
    String programPrefix = null;
    boolean firstRun = false;
    String sourceFolder = "";

    Properties prop = new Properties();
    InputStream input = null;

    try {

      input = new FileInputStream("cdf.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      appPath = prop.getProperty("appPath");
      libFile = prop.getProperty("libFile");
      entryPointFile = prop.getProperty("entryPointFile");
      settingLoadingMethodFile = prop.getProperty("settingLoadingMethodFile");
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
    System.out.println("METHOD " + Util.getMethodName(optionSource));
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
    Util.readFile(settingLoadingMethodFile, tmpRawSources);
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

    List<String> sinks = new ArrayList<String>();
    // {
    // {
    // // add("<test.Main: int foo2()>");
    // // add("<test.Main: void sink(java.lang.String)>");
    // // add("<org.apache.cassandra.service.CassandraDaemon: void main(java.lang.String[])>"); //
    // // for
    // // Cassandra
    // // add("<org.apache.derby.tools.PlanExporter: void main(java.lang.String[])>"); // this
    // // function is
    // // random and not
    // // importatn
    // // add("<org.elasticsearch.bootstrap.Elasticsearch: void main(java.lang.String[])>");
    // add("");
    // // add("<test.Main: int secret()>");
    // }
    // };
    sinks.add(optionSink);
    FileWriter infoWriter = null;
    try {
      infoWriter = new FileWriter(version + "-result/" + "information.txt");
      for (String source : wholeSources) {
        System.out.println("PARSING " + source);

        // TODO: temporarily disabled
        // IInfoflow infoflow =
        // new NhanInfoflow(version, getMethodName(source), programPrefix, firstRun, sourceFolder,
        // infoWriter);
        List<String> sources = new ArrayList<String>();
        sources.add(source);
        // infoflow.computeInfoflow(appPath, libPath, entryPointList, sources, sinks);

        IInfoflow infoflow = new Infoflow(version, Util.getMethodName(source), 1);
        infoflow.computeInfoflow(appPath, libPath, entryPointList, sources, sinks);

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
