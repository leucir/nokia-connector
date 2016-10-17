package com.ibm.iot.connector.domain;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

public class MessageOutDevice {

	public String deviceId;
	public String authToken = "12345_ASWss";
	public DeviceTypeDeviceInfo deviceInfo;
	public DeviceLocation location;
	public DeviceTypeMetadata metadata;

	//public String platformId = "Impact";

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
	
}
