package com.ibm.iot.connector.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HTTPClient {
	private static String CLASS_NAME = HTTPClient.class.getName();
	private static final Logger LOGGER = Logger.getLogger(HTTPClient.class.getName());
	private static final String BASIC_HTTP_AUTH_HEADER = "Basic";
	

	public HttpResponse submitRequest(HttpRequestBase httpRequest) throws RuntimeException {
		final String METHOD_NAME = "submitRequest"; //$NON-NLS-1$

		CloseableHttpClient httpClient;
		httpClient = HttpClientBuilder("https");
		
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpRequest);
		} catch (IOException e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "*** ERROR *** Could not submit the Delete Callback HTTP request to external platform.",	e.getMessage());			
			throw new RuntimeException("Could not submit the HTTP request to external platform.", e);
		}
		LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "HTTP Request submitted to external platform.");
		return response;
	}
	
	
	private CloseableHttpClient HttpClientBuilder(String protocol) {
		final String METHOD_NAME = "HttpClientBuilder"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {protocol });
		LOGGER.logp(Level.FINER, CLASS_NAME, METHOD_NAME, "instantiated.");
		
		CloseableHttpClient httpClient = null;

		if (protocol.equalsIgnoreCase("https")) {
			SSLContext sslContext = null;
			SSLConnectionSocketFactory sslConfactory = null;
			try {
				sslContext = SSLContexts.custom().useTLS().build();
				sslConfactory = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null,
						verifier());

			} catch (KeyManagementException | NoSuchAlgorithmException e) {
				LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "SSL error connecting to the external platform");				
			}

			httpClient = HttpClients.custom().setSSLSocketFactory(sslConfactory).build();
		}

		if (protocol.equalsIgnoreCase("http")) {
			httpClient = HttpClients.createDefault();
		}

		return httpClient;
	}
	
	private X509HostnameVerifier verifier() {
		X509HostnameVerifier verifier = new AbstractVerifier() {
			@Override
			public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException {
				verify(host, cns, subjectAlts, true);
			}
		};

		return verifier;
	}
	
	public Map<String, String> extractFromRequest(MultivaluedMap<String, String> map) {
		Map<String, String> resultMap = new HashMap<String, String>();

		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String theKey = (String) it.next();
			resultMap.put(theKey, map.getFirst(theKey));
		}
		return resultMap;
	}
	
	public String extractContentfrom(HttpResponse response) throws Exception {
		final String METHOD_NAME = "extractContentfrom"; //$NON-NLS-1$

		HttpEntity responseEntity = response.getEntity();
		String content = null;
		try {
			// Check if response is empty
			if (responseEntity == null) {
				content = "";
			} else {
				content = EntityUtils.toString(responseEntity);
			}
		} catch (ParseException | IOException e) {
			LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Parsing response failed.", e);	
			throw new RuntimeException("Parsing response failed.", e);			
		} 
		return content;
	}
	
	public String getBasicAuthHeader(String user, String pass){
		final String METHOD_NAME = "getBasicAuthHeader"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		//Mount APIKey BASIC Header
		String authString = 
				user + 
				":" + 
				pass;
		
		byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		
		StringBuilder basicAuth = new StringBuilder();
		basicAuth.append(HTTPClient.BASIC_HTTP_AUTH_HEADER)
		.append(" ")
		.append(authStringEnc);
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return basicAuth.toString();
	}
	
}
