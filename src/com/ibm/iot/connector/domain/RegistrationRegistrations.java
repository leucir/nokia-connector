package com.ibm.iot.connector.domain;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

public class RegistrationRegistrations extends ComponentBase{

	public String serialNumber;
	public String timestamp;
	public String make;
	public String model;
	public String firmwareVersion;
	public String groupName;
	public String imsi;
	public String address;
	public String protocol;
	public String tags;
	public String subscriptionId;

	//event type definition
	//This value is used on MQTT publish function
	public transient String eventType = "registration";
	
	public byte[] decode64(String content) throws UnsupportedEncodingException{
		return DatatypeConverter.printBase64Binary(content.getBytes("UTF-8")).getBytes();
	}

	/**
	 * Returns the JSON for the notification
	 * @return
	 */
	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public String getFormattedSerialNumber(){
		String newSerial = this.serialNumber.replaceAll(":", "-");
		return newSerial;
	}
	
}
