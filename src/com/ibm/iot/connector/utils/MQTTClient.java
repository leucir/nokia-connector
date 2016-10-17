package com.ibm.iot.connector.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTClient {

	private static final String CLASS_NAME = MQTTClient.class.getName();
	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private static final String MESSAGING_PREFIX = "messaging";
	public static final String CONNECTOR_TYPE = "connector";
	public static final String CONNECTOR_DEVICE_CLASS = "c";
	public static final String APPLICATION_DEVICE_CLASS = "a";

	private static String JKS_PASSWORD = null;
	private static String JKS_FILE_PATH = null;
	
	private MqttClient client = null;
		
	//Load IoTP connection properties from configuration file into static variables
	private static String environment;
	private static int qosProp = 0;
	private static boolean cleanSessionProp = true;
	private static String serverURIProp;
	private static boolean messageRetainProp = false;
	private static String authmethodProp;
	
	private static Properties props = new Properties();
	static{
		
		//Mount properties full name based on connectorId and connectorVersion and load it
		InputStream in = MQTTClient.class.getResourceAsStream("mqtt.properties");
		try {
			props.load(in);
			
			environment = props.getProperty("environment"); 
			qosProp = Integer.valueOf(props.getProperty("qos").trim()).intValue();
			cleanSessionProp = Boolean.getBoolean(props.getProperty("cleanSession").trim());
			serverURIProp = props.getProperty("serverURI");
			messageRetainProp = Boolean.getBoolean(props.getProperty("messageRetain").trim());
			authmethodProp = props.getProperty("deviceAuthType");
			
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, "static", "Cannot retrieve configuration file from publisher - mqtt.properties .", e);
		}
		//Retrieves JKS configuration from Consul
		SetJKSConfig();
	}
	

	/**
	 * Return a new instance of the Mqtt client
	 * @param ORG-ID inside Map<String,String>
	 * @param CLIENT-ID inside Map<String,String>
	 * @param forceNew Indicates if a new connection is always required. Not using connection in memory.
	 * @return
	 * @throws Exception 
	 */
	public MQTTClient getInstance(String orgId, String platformId, boolean forceNew) throws Exception{
		final String METHOD_NAME = "newInstance"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		
		String finalURL = this.getMessagingUrl(orgId);
		
		try {
			//Only get a new client if the existent reference is null or forceNew is true
			if(client == null || forceNew){
				MemoryPersistence persistence = new MemoryPersistence();
				
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "MQTT SERVICE URL: {0} and CLIENT_ID: {1}", new Object[] {finalURL, getClientId(orgId, platformId)});		
				
				client = new MqttClient(finalURL, getClientId(orgId, platformId), persistence);	
			}
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Not able to create an instance of MqttClient.", e);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME);
			throw e;
		}	
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return this;
	}
	
	/**
	 * 
	 * @param topic
	 * @return
	 * @throws Exception 
	 */
	public boolean subscribeEvents(String topic) throws Exception{
		final String METHOD_NAME = "subscribeEvents"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);

		//Try set subscribe and callback
		try{
			client.subscribe(topic);
			client.setCallback(new MQTTCLientCallback());
		}catch (Exception ex){
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Not able to create an instance of MqttClient.", ex);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME);
			throw ex;
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME, true);
		return true;		
	}
	
	
	/**
	 * Publish events to IoTP
	 * @param topic
	 * @param payload
	 * @return
	 * @throws Exception 
	 */
	public boolean publishEvents(String username, char[] password, String topic, byte[] payload) throws Exception{
		final String METHOD_NAME = "publishEvents"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		
		MqttConnectOptions options = new MqttConnectOptions();
		
		//options = this.setSSLContext(JKS_FILE_PATH, JKS_PASSWORD, options);
		
		options.setCleanSession(cleanSessionProp);
		options.setUserName(username);
		options.setPassword(password);
		
		//Define SSL as default
		java.util.Properties sslClientProps = new java.util.Properties();
		sslClientProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
		options.setSSLProperties(sslClientProps);		
		
		//Connect to IoTP
		try {
			if(!client.isConnected()){
				client.connect(options);
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "MQTT Connection is established.");
			}else{
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "MQTT is already connected.");
			}
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Cannot connect to IoTP using MQTT.", e);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME);
			throw e;
		}
			
		//After connect, publish events
		try {
			client.publish(topic, payload, qosProp, messageRetainProp);
			
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Notification has been published.");
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Cannot publish messages to IoTP using MQTT.", e);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME, false);
			return false;
		}
		
		//Try disconnect from IoTP
		//this.disconnect();
		LOGGER.exiting(CLASS_NAME, METHOD_NAME, true);
		return true;		
		
	}
	
	public boolean disconnect(){
		final String METHOD_NAME = "disconnect"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		
		try {
			if(client.isConnected())
				client.disconnect()	;
		} catch (Exception e) {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Cannot disconnect connect from IoTP", e);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME, false);
			return false;
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME, true);
		return true;
	} 
	
	
	/**
	 * Mount clientId based on configuration and dynamic attributes
	 * CliendId is c:<org>:<deviceType>:<instanceId>:<variation>
	 * @param config
	 * @return
	 */
	public String getClientId(String orgId, String platformId){
		final String METHOD_NAME = "getClientId"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		//Ensure temporary uniqueness for CliendId
		//InstanceId is not enough, many HTTP instances, coming from an external system can use the same connector instanceId
		//Using a timestamp for that
		Date currDate = new Date();
		long variation = currDate.getTime();
				
		StringBuilder clientId = new StringBuilder();
		
		clientId
			.append(MQTTClient.APPLICATION_DEVICE_CLASS)
			.append(":")
			.append(orgId)
			.append(":")
			//.append(platformId)
			//.append(":")
			.append(String.valueOf(variation));
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return clientId.toString();
	}
	

	/**
	 * Return a MQTT Messaging url to be used according the current domain
	 * @param orgId
	 * @return
	 * @throws Exception 
	 */
	protected String getMessagingUrl(String orgId) throws Exception{
		final String METHOD_NAME = "getMessagingUrl"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		
		
		String env = MQTTClient.MESSAGING_PREFIX + "." + MQTTClient.environment;
		
		String finalURL = serverURIProp.replaceAll("<environment>", env); ;
		finalURL = finalURL.replaceAll("<org_id>",orgId);

		LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Watson IoT Messaging URL :: {0}", new Object[] {finalURL});
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		
		return finalURL;
	}
	
	
	/**
	 * Manage SSL for MQTT
	 * @param jksFIle
	 * @param jksPassword
	 * @param connOpts
	 * @return
	 * @throws Exception
	 */
	public MqttConnectOptions setSSLContext(String jksFIle, String jksPassword, MqttConnectOptions connOpts) throws Exception {
		final String METHOD_NAME = "setSSLContext"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(jksFIle), jksPassword.toCharArray());

		// all we need is trust...
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ks);

		// IoTF supports TLSv1.2
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

		sslContext.init(null, tmf.getTrustManagers(), null);
		SSLSocketFactory factory = sslContext.getSocketFactory();

		connOpts.setSocketFactory(factory);
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return connOpts;

	}  
	

	/**
	 * @throws Exception 
	 */
	protected static void SetJKSConfig(){		
		final String METHOD_NAME = "SetJKSConfig";
		LOGGER.entering(CLASS_NAME, METHOD_NAME);
		
		try{
			String trustStorePath = "";
			String trustStorePassword = "";
			
			JKS_FILE_PATH = trustStorePath;
			JKS_PASSWORD = trustStorePassword;
			
		}catch(Exception ex){
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
					"Unable to set JKS environment variables.", ex);
			LOGGER.exiting(CLASS_NAME, METHOD_NAME);
			throw new RuntimeException("Unable to set JKS environment variables.");
		}
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}

	
}
