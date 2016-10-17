package com.ibm.iot.connector.domain;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.ibm.iot.connector.core.ExternalSystem;
import com.ibm.iot.connector.utils.HTTPClient;
import com.ibm.iot.connector.utils.MQTTClient;

public class WatsonIoT extends ExternalSystem {
	private static String CLASS_NAME = WatsonIoT.class.getName();
	private static final Logger LOGGER = Logger.getLogger(WatsonIoT.class.getName());

	public String orgId;
	public String apiKey;
	public String apiToken;
	
	public WatsonIoT() {
		super.id = "watson";
	}
	
	@Override
	public void setEndPointUrl(String endpoint) {
		super.endpointUrl = endpoint;
	}

	@Override
	public String getId() {
		return super.id;
	}

	@Override
	public String getEndPointUrl() {
		return "https://" +
				this.orgId +
				".internetofthings.ibmcloud.com/api/v0002/";
	}

	@Override
	public void setHeader(Map<String, String> headers) {
		super.headers=headers;		
	}

	@Override
	public String processCallbackResponse(String strExtSysResponse) {
		
		return null;
	}

	@Override
	public String processCallbackRequest(String body) {
		// TODO Auto-generated method stub
		return null;
	}

	public void publishEvents(String platformId, RegistrationReports[] reports) {
		final String METHOD_NAME = "publishEvents"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {});
		
		MQTTClient mqttClient = new MQTTClient();
		
		//Read watson properties - in this case, only to get the publish topic
		Properties props = super.readSystemProperties();
		
		try {
			//Get an instance/connection for MQTT
			mqttClient = mqttClient.getInstance(this.orgId, platformId, true);
				
			for (RegistrationReports report : reports) {
				String topicName = props.getProperty("publishConnectorTopic");
				topicName = topicName.replaceAll("<device_type>", report.ept);
				topicName = topicName.replaceAll("<device_id>", report.getFormattedSerialNumber());
				topicName = topicName.replaceAll("<event_type>", report.eventType);
				topicName = topicName.replaceAll("<format>", "json");
				
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Publishing messages to {0}", new Object[] {topicName});
				
				mqttClient.publishEvents(this.apiKey, this.apiToken.toCharArray(), topicName, report.toJson().getBytes());
				LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Messages Published...");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
	}

	public boolean createDevices(String id, RegistrationRegistrations[] registrations) {
		final String METHOD_NAME = "createDevices"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {});

		//Create device type
		RegistrationRegistrations reg = registrations[0];
		
		MessageOutDeviceType deviceType = new MessageOutDeviceType();
		deviceType.id = reg.ept;
		deviceType.description = "Generic Impact Device";
		deviceType.classId = "Device";
		
		this.createDeviceType(deviceType);
		
		
		//Create devices
		HTTPClient httpClient = new HTTPClient();
		HttpPost httpPost = new HttpPost(this.getEndPointUrl() +
											"device/types/" +
											reg.ept +
											"/devices");

		
		for (RegistrationRegistrations registration : registrations) {
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Creating a new device {0} on WatsonIoT", new Object[] {registration.serialNumber});
			
			
			MessageOutDevice device = new MessageOutDevice();
			device.deviceId = registration.getFormattedSerialNumber();
			
			DeviceTypeDeviceInfo deviceInfo = new DeviceTypeDeviceInfo();
			deviceInfo.description = registration.model;
			deviceInfo.fwVersion = registration.firmwareVersion;
			deviceInfo.manufacturer = registration.make;
			deviceInfo.model = registration.model;

			device.deviceInfo = deviceInfo;
			
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Device JSON prepared to be processed {0}", new Object[] {device.toJson()});
			
			HttpEntity entity = null;
			entity = new StringEntity(device.toJson(), ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);

			httpPost.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
			httpPost.setHeader("Authorization", httpClient.getBasicAuthHeader(this.apiKey, this.apiToken));

			HttpResponse extSysResponse = httpClient.submitRequest(httpPost);
			try {
				String strExtSysResponse = httpClient.extractContentfrom(extSysResponse);
				this.processDeviceCreateResponse(strExtSysResponse);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Device created...");
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return true;
	}

	private void processDeviceCreateResponse(String strExtSysResponse) {
		final String METHOD_NAME = "processDeviceCreateResponse"; //$NON-NLS-1$
		
		LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Device Create Response {0}", new Object[] {strExtSysResponse});		
	}

	private boolean createDeviceType(MessageOutDeviceType deviceType){
		final String METHOD_NAME = "createDeviceType"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {});
		
		HTTPClient httpClient = new HTTPClient();
		HttpPost httpPost = new HttpPost(this.getEndPointUrl() + "device/types");
		
		LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "DeviceType JSON prepared to be processed {0}", new Object[] {deviceType.toJson()});
		
		HttpEntity entity = null;
		entity = new StringEntity(deviceType.toJson(), ContentType.APPLICATION_JSON);
		httpPost.setEntity(entity);
		
		httpPost.setHeader("Content-type", ContentType.APPLICATION_JSON.toString());
		httpPost.setHeader("Authorization", httpClient.getBasicAuthHeader(this.apiKey, this.apiToken));
		
		HttpResponse extSysResponse = httpClient.submitRequest(httpPost);
		try {
			String strExtSysResponse = httpClient.extractContentfrom(extSysResponse);
			this.processDeviceTypeCreateResponse(strExtSysResponse);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return true;
	}

	private void processDeviceTypeCreateResponse(String strExtSysResponse) {
		final String METHOD_NAME = "processDeviceTypeCreateResponse"; //$NON-NLS-1$
		
		LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "DeviceType Create Response {0}", new Object[] {strExtSysResponse});	
	}
	
}
