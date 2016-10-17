package com.ibm.iot.connector.domain;

import com.google.gson.annotations.SerializedName;

public class Registration {

	public String ep;
	
	public String ept;
	
	@SerializedName("resources")
	public Resource[] resources;
	
}
