package com.kjlee.java_server.controller;

import com.kjlee.java_server.vo.Employee;
import com.kjlee.java_server.vo.MyFiles;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.security.cert.CertificateException;

@RestController
public class HomeController {

    @GetMapping("/testcall")
    Employee testcall() {
        Employee employee = new Employee();
        employee.setName("hong");
        employee.setAge("40");
        return employee;
    }

    @GetMapping("/asperacall")
    MyFiles asperacall() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        MyFiles files = new MyFiles();
        // aspera call

//        String JSONInput = ("{\n" +
//                "    \"path\": \"/\"" +
//                "} ");
//        HttpHeaders headers = createHeaders("node_xfer", "xfer");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity param= new HttpEntity(JSONInput, headers);
////        RestTemplate restTemplate = new RestTemplate();
//        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
//        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(httpClient);
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//        String result = restTemplate.postForObject("https://192.168.0.12:9092/files/browse", param, String.class);

        String result = asperaService();
//        System.out.println(result);

        files.setList(result);
        return files;
    }

//    HttpHeaders createHeaders(String username, String password){
//        return new HttpHeaders() {{
//            String auth = username + ":" + password;
//            byte[] encodedAuth = Base64.encodeBase64(
//                    auth.getBytes(Charset.forName("US-ASCII")) );
//            String authHeader = "Basic " + new String( encodedAuth );
//            set( "Authorization", authHeader );
//        }};
//    }

    String asperaService(){
        try {
            String api = "browse";
            URL url = new URL("https://192.168.0.12:9092/files/" + api);

            ignoreSSL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            String name = "node_xfer";
            String password = "xfer";

            String authString = name + ":" + password;
            java.util.Base64.Encoder encoder = Base64.getEncoder();
            byte[] authEncBytes = encoder.encode(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            con.setRequestProperty("Authorization", "Basic " + authStringEnc);

            con.setRequestMethod("POST");

            con.setRequestProperty("Content-Type", "application/json");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setDefaultUseCaches(false);

            String json = null;
            String current = "/";
            json = "{\"path\":\"" + current + "\",\"sort\": \"type_a\"}";

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
            wr.write(json);
            wr.flush();

            /////////////////////////////////////////////////////////////////////////////////////////

            StringBuilder sb = new StringBuilder();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
            } else {
                System.out.println(con.getResponseMessage());
            }

            return sb.toString();
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
    }

    public void ignoreSSL() throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };

        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

    }

    static class miTM implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            return;
        }
    }
}
