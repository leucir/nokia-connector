package com.ibm.iot.connector.domain;

import com.google.gson.Gson;

public class ObservationResources {

	public String resourcePath;	
	public String value;
	
	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
}
