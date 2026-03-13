package org.modelseeed.rast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class RPCClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String url;
    private final String token;
    private final String version;
    private final Duration timeout;
    //private final boolean trustAllSslCertificates;
    private final HttpClient httpClient;

    public RPCClient(String url) {
        this(url, null, "1.0", Duration.ofMinutes(30), false);
    }

    public RPCClient(String url, String token) {
        this(url, token, "1.0", Duration.ofMinutes(30), false);
    }

    public RPCClient(
            String url,
            String token,
            String version,
            Duration timeout,
            boolean trustAllSslCertificates
    ) {
        this.url = url;
        this.token = token;
        this.version = version;
        this.timeout = timeout;
        //this.trustAllSslCertificates = trustAllSslCertificates;
        this.httpClient = buildHttpClient(timeout, trustAllSslCertificates);
    }

    public AnnotationRASTResult call(String method, Object params) throws IOException, InterruptedException {
        return call(method, params, null);
    }

    public AnnotationRASTResult call(String method, Object params, String overrideToken)
            throws IOException, InterruptedException {

        Map<String, Object> argHash = new LinkedHashMap<>();
        argHash.put("method", method);
        argHash.put("params", params);
        argHash.put("version", version);
        argHash.put("id", randomId());
        argHash.put("context", new LinkedHashMap<String, Object>());

        String body;
        try {
            body = OBJECT_MAPPER.writeValueAsString(argHash);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to serialize RPC request body", e);
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));

        String authToken = overrideToken != null ? overrideToken : this.token;
        if (authToken != null && !authToken.isEmpty()) {
            requestBuilder.header("AUTHORIZATION", authToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body() != null ? response.body() : "";
        String contentType = response.headers()
                .firstValue("content-type")
                .orElse("")
                .toLowerCase();

        if (statusCode == 500) {
            if (contentType.contains("application/json")) {
              AnnotationRASTResult err = parseJsonObject(responseBody);
              /**
                Object errorObj = err.get("error");

                if (errorObj instanceof Map<?, ?> errorMapRaw) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> errorMap = (Map<String, Object>) errorMapRaw;
                    throw new IOException(errorMap.toString());
                } else {
                    throw new IOException(responseBody);
                }
                **/
            } else {
                throw new IOException(responseBody);
            }
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP error " + statusCode + ": " + responseBody);
        }

        AnnotationRASTResult resp = parseJsonObject(responseBody);

        return resp;
    }

    private static AnnotationRASTResult parseJsonObject(String json) 
        throws JsonMappingException, JsonProcessingException  {
        return AnnotationRASTResult.fromJson(json, OBJECT_MAPPER);
        //OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    private static String randomId() {
        long n = Math.abs(new Random().nextLong());
        return Long.toString(n);
    }

    private static HttpClient buildHttpClient(Duration timeout, boolean trustAllSslCertificates) {
      HttpClient.Builder builder = HttpClient.newBuilder()
              .connectTimeout(timeout);

      if (trustAllSslCertificates) {
          try {
              SSLContext sslContext = createTrustAllSslContext();
              SSLParameters sslParameters = new SSLParameters();
              // Disable hostname verification
              sslParameters.setEndpointIdentificationAlgorithm("");

              builder.sslContext(sslContext);
              builder.sslParameters(sslParameters);
          } catch (GeneralSecurityException e) {
              throw new RuntimeException("Failed to create trust-all SSL context", e);
          }
      }

      return builder.build();
  }

    private static SSLContext createTrustAllSslContext() throws GeneralSecurityException {
        TrustManager[] trustAllManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllManagers, new SecureRandom());
        return sslContext;
    }
}