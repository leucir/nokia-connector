package com.ibm.iot.connector.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ExternalSystem {
	private static String CLASS_NAME = ExternalSystem.class.getName();
	private static final Logger LOGGER = Logger.getLogger(ExternalSystem.class.getName());

	public String id;
	public String endpointUrl;
	public Map<String, String> headers;

	public abstract String getId();

	public abstract void setEndPointUrl(String endpoint);
	public abstract String getEndPointUrl();

	public abstract void setHeader(Map<String, String> headers);

	public abstract String processCallbackResponse(String strExtSysResponse);
	public abstract String processCallbackRequest(String body);

	public Properties readSystemProperties(){
		final String METHOD_NAME = "readSystemProperties";

		InputStream in = ExternalSystem.class.getResourceAsStream(this.id + ".properties");
		Properties props = new Properties();

		try {
			props.load(in);

		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Cannot retrieve configuration file from "+ this.id, e);
		}

		return props;
	}

}
