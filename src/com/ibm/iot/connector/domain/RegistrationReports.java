package com.ibm.iot.connector.domain;

import java.io.UnsupportedEncodingException;
import javax.xml.bind.DatatypeConverter;
import com.google.gson.Gson;

public class RegistrationReports extends ComponentBase{

	public String serialNumber;
	public long timestamp;
	public String subscriptionId;
	public String resourcePath;
	public String value;
	
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
