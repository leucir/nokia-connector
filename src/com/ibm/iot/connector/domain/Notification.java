package com.ibm.iot.connector.domain;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Notification extends ComponentBase{

	public String ep;
	
	public String path;
	
	public String ct;
	
	public String payload;
	
	@SerializedName("max-age")
	public String maxAge;
	
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
