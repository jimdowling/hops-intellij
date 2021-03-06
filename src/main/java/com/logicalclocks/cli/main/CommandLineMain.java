package com.logicalclocks.cli.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.logicalclocks.cli.action.FileUploadAction;
import com.logicalclocks.cli.config.HopsworksAPIConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * *
 * <p>
 * Resources:
 *  * https://starkandwayne.com/blog/working-with-hdfs/
 *  * https://github.com/Microsoft/hdfs-mount
 * <p>
 * <p>
 */
public class CommandLineMain {

  private static final String JOBS = "jobs";
  private static final String FS = "fs";

  private static final String HOPSWORKS_PROJECT = "HOPSWORKS_PROJECT";
  private static final String HOPSWORKS_URL = "HOPSWORKS_URL";
  private static final String HOPSWORKS_APIKEY = "HOPSWORKS_APIKEY";
  private static final String path = "/hopsworks-api/api";

  /**
   * *
   * These properties come either from environment variables or from
   * properties set in the conf/hops.properties file.
   * Environment variables take precedence over values in hops.properties.
   */
  private static final Properties props = new Properties();
  public static String project;
  public static String hopsworksUrl;
  public static String hopsworksApiKey;

  /**
   * There are 2 levels of commands.
   * 1. The top level commands (Args) - fs, jobs
   * 2. Sub-commands for each of the top-level commands (FsArgs, JobsArgs).
   */
  
  public static class Args {

    @Parameter(description = "")
    public List<String> mainArgs;

    @Parameter(names = FS,
        description = "Filesystem commands",
        order = 0)
    public String fsArgs;

    @Parameter(names = JOBS,
        description = "Job commands",
        order = 1)
    public String jobsArgs;

    @Parameter(names = "-conf",
        description = "Location of hops.properties config file",
        order = 2)
    public String conf = "conf/hops.properties";

    @Parameter(names = "-proj",
        description = "Name of the project",
        order = 3)
    public String project;

    @Parameter(names = "-help",
        description = "Usage of hops-cli",
        help = true,
        order = 4)
    public boolean help;
  }

  public static class JobsArgs {

    @Parameter(names = "submit",
        description = "Submit a python file (.py) or a jar file (.jar) for execution.")
    public String submit;

    @Parameter(names = "remove",
        description = "Remove a job with given id.")
    public int removeJobId;

    @Parameter(names = "logs",
        description = "Download the latest logs for a job with given id.")
    public int logsJobId;

    @Parameter(names = "-name",
        description = "Job ")
    public String jobName = "hops-cli";
    @Parameter(names = "--driver-memory",
        description = "Job ")
    public String driverMemory = "1024M";
    @Parameter(names = "--driver-cores",
        description = "Job ")
    public String driverCores = "1";
    @Parameter(names = "--num-executors",
        description = "")
    public String numExecutors = "1";
    @Parameter(names = "--spark-properties",
        description = "")
    public String SparkProps = "";
    @Parameter(names = "--executor-memory",
        description = "")
    public String executorMemory = "2048M";
    @Parameter(names = "--executor-cores",
        description = "")
    public String executorCores = "4";
    @Parameter(names = "--executor-gpus",
        description = "")
    public String executorGpus = "0";

  }

  public static class FsArgs {

//    @Parameter(names = "-ls",
//        description = "List files/dirs in the given path")
//    public String lsPath;
//
//    @Parameter(names = "-rm",
//        description = "Remove a files in the given path")
//    public String rmPath;
//
//    @Parameter(names = "-mkdir",
//        description = "Make a directory in the given path")
//    public String mkdirPath;

    @Parameter(names = "-copyFromLocal",
        arity = 2,
        description = "Copy a file from the local filesystem to the remote")
    public List<String> copyFromLocal;

//    @Parameter(names = "-copyToLocal",
//        arity = 2,
//        description = "Copy a file from the local filesystem to the remote")
//    public List<String> copyToLocal;
//
//    @Parameter(names = "-copyFromHdfs",
//        arity = 2,
//        description = "Copy a file from a local HDFS/HopsFS cluster to a remote Hops cluster")
//    public List<String> copyFromHdfs;

  }

  private static String getProperty(String variable, String env) throws Exception {
    if (variable == null) {
      variable = props.getProperty(env);
    }
    return variable;

  }


  public static class Test {
    public static void exec() throws IOException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {

      HttpClient httpclient = HttpClients
              .custom()
              .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null,
                      TrustSelfSignedStrategy.INSTANCE).build())
              .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
              .build();

        HttpPost httppost = new HttpPost("https://35.185.114.183/hopsworks-api/api/project/119/" +
                "dataset/upload/Resources");

        FileBody bin = new FileBody(new File("C:\\Users\\Jim Dowling\\IdeaProjects\\hopsworks-cli\\pom.xml"));
        // the API key here
        StringBody comment = new StringBody(
                "pTpaLQJFkfkxwgPN.YU2Q4Dl1zj70J0qRSYS3gdIjOU2yoIPU7qofJwy2eFaFcFfZoFbXOVIJvm9K7iEW",
                ContentType.TEXT_PLAIN);

        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addPart("ApiKy", comment)
                .addPart("file", bin)
                .build();

        httppost.addHeader("Authorization" ,
                "ApiKey pTpaLQJFkfkxwgPN.YU2Q4Dl1zj70J0qRSYS3gdIjOU2yoIPU7qofJwy2eFaFcFfZoFbXOVIJvm9K7iEW");

        httppost.setEntity(reqEntity);

        System.out.println("executing request " + httppost.getRequestLine());
        HttpResponse response = httpclient.execute(httppost);
          System.out.println("----------------------------------------");
          System.out.println(response.getStatusLine());
          HttpEntity resEntity = response.getEntity();
          if (resEntity != null) {
            System.out.println("ToString:" + EntityUtils.toString(resEntity));
          }
          EntityUtils.consume(resEntity);

    }
  }


  public static void main(String[] args) {

    System.setProperty("java.net.preferIPv4Stack", "true");

//    try {
//      Test.exec();
//    } catch (IOException e) {
//      e.printStackTrace();
//    } catch (NoSuchAlgorithmException e) {
//      e.printStackTrace();
//    } catch (KeyStoreException e) {
//      e.printStackTrace();
//    } catch (KeyManagementException e) {
//      e.printStackTrace();
//    }
//    System.exit(0);



    // Priority for configuration variables/args (1 is highest, 3 is lowest): 
    // 1. Command Line > 2. Environment Variables > 3. conf/hops.properties
    project = System.getenv().get(HOPSWORKS_PROJECT);
    hopsworksUrl = System.getenv().get(HOPSWORKS_URL);
    hopsworksApiKey = System.getenv().get(HOPSWORKS_APIKEY);

    Args a = new Args();
    JCommander jc = JCommander.newBuilder()
        .addObject(a)
        .build();
    jc.setProgramName("hops-cli");

    if (args.length == 0) {
      jc.usage();
      System.exit(0);
    }

    try {
      jc.parse(args);
    } catch (ParameterException ex) {
      jc.usage();
      System.exit(0);

    }

    if (a.help) {
      jc.usage();
      System.exit(1);
    }

    String configFile = a.conf;

    try {
      props.load(new FileInputStream(configFile));
      project = CommandLineMain.getProperty(project, HOPSWORKS_PROJECT);
      hopsworksUrl = CommandLineMain.getProperty(hopsworksUrl, HOPSWORKS_URL);
      hopsworksApiKey = CommandLineMain.getProperty(hopsworksApiKey, HOPSWORKS_APIKEY);
    } catch (IOException ex) {
      System.err.println("Problem reading/parsing the conf/hops.properties file.");
      System.exit(-1);
    } catch (Exception ex) {
      Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
      System.exit(-1);
    }

    if (a.project != null) {
      project = a.project;
    }
    // Only one of the top-level commands should be active (not allowed -> both null, both non-null)
    if ((a.jobsArgs == null && a.fsArgs == null) || (a.jobsArgs != null && a.fsArgs != null)) {
      jc.usage();
      System.exit(1);
    }

    JobsArgs jobsArgs = new JobsArgs();
    FsArgs fsArgs = new FsArgs();

    JCommander jcJob = JCommander.newBuilder()
        .addObject(jobsArgs)
        .build();
    jcJob.setProgramName("hops-cli " + JOBS);

    JCommander jcFs = JCommander.newBuilder()
        .addObject(fsArgs)
        .build();
    jcFs.setProgramName("hops-cli " + FS);

    
    try {
      if (a.jobsArgs != null) {
        JCommander jcJobs = JCommander.newBuilder()
            .addObject(jobsArgs)
            .build();
        jcJobs.setProgramName("jobs");

        if (a.mainArgs == null) {
          jcJob.usage();
          System.exit(2);
        }
        a.mainArgs.add(0, a.jobsArgs);
        String[] commandArgs = a.mainArgs.toArray(new String[0]);
        jcJobs.parse(commandArgs);
      } else if (a.fsArgs != null) {
        if (a.mainArgs == null) {
          jcFs.usage();
          System.exit(2);
        }
        a.mainArgs.add(0, a.fsArgs);
        String[] commandArgs = a.mainArgs.toArray(new String[0]);
        jcFs.parse(commandArgs);

        String authPath = "/auth/login";
        URL url = null;
        InetAddress address = null;
        try {
          url = new URL(hopsworksUrl);
          address = InetAddress.getByName(url.getHost());
        } catch (MalformedURLException ex) {
          Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
          jc.usage();
          System.exit(1);
        } catch (UnknownHostException ex) {
          Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
          jc.usage();
          System.exit(1);
        }
        String hostname = address.getHostName();
        int port = url.getPort();

        if (fsArgs.copyFromLocal != null) {
          String relativePath = fsArgs.copyFromLocal.get(0);
//          String absolutePath = FileSystems.getDefault().getPath(relativePath).normalize().toAbsolutePath().toString();
          String datasetPath = fsArgs.copyFromLocal.get(1);

          try {
            HopsworksAPIConfig hopsworksAPIConfig = new HopsworksAPIConfig( hopsworksApiKey, hopsworksUrl, project);
            FileUploadAction action = new FileUploadAction(hopsworksAPIConfig, datasetPath, relativePath);
            action.execute();
          } catch (IOException ex) {
            Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
            jc.usage();
            System.exit(1);
          } catch (URISyntaxException ex) {
            Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
            jc.usage();
            System.exit(1);
          } catch (Exception ex) {
            Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
            jc.usage();
            ex.printStackTrace();
            System.exit(1);
          }

        }

      } else {
        jc.usage();
        System.exit(1);
      }
    } catch (ParameterException ex) {
      jc.usage();
      System.exit(2);
    }

    URI uri = null;
    try {
      uri = new URI(hopsworksUrl);
    } catch (URISyntaxException ex) {
      Logger.getLogger(CommandLineMain.class.getName()).log(Level.SEVERE, null, ex);
      System.exit(-2);
    }

  }

}
