package com.logicalclocks.cli.action;

import com.logicalclocks.cli.config.HopsworksAPIConfig;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.apache.http.HttpHeaders.USER_AGENT;

public abstract class HopsworksAction {

  final HopsworksAPIConfig hopsworksAPIConfig;

  public HopsworksAction(HopsworksAPIConfig hopsworksAPIConfig ){
    this.hopsworksAPIConfig = hopsworksAPIConfig;
  }

  public  String getProjectId(HopsworksAPIConfig apiConfig) throws IOException {
    HttpClient client = getClient();

    HttpGet request = new HttpGet(apiConfig.getProjectNameUrl());
    request.addHeader("User-Agent", USER_AGENT);
    request.addHeader("Authorization", "ApiKey " + apiConfig.getApiKey());
    HttpResponse response = client.execute(request);
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    JsonObject body = Json.createReader(new StringReader(result.toString())).readObject();
    JsonValue projectId = body.get("projectId");

    return projectId.toString();
  }

  protected HttpClient getClient() throws IOException {
    HttpClient getClient = null;
    try {
      getClient = HttpClients
              .custom()
              .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null,
                      TrustSelfSignedStrategy.INSTANCE).build())
              .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
              .build();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    } catch (KeyManagementException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    } catch (KeyStoreException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
    return getClient;
  }

  public static URI pathToUri(String filePath) throws URISyntaxException {
    URI uri;
    // Default FS if not given, is the local FS (file://)
    if (SystemUtils.IS_OS_WINDOWS && filePath.startsWith("file://") == false) {
      uri = new URI("file:///" + filePath);
    } else if (filePath.startsWith("/")) {
      uri = new URI("file://" + filePath);
    } else {
      uri = new URI(filePath);
    }
    return uri;
  }

  /**
   * Send http request.
   *
   * @return http status code.
   */
  public abstract int execute() throws Exception;
}
