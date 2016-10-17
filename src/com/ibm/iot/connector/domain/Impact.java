package com.ibm.iot.connector.domain;

import java.util.Map;

import com.google.gson.Gson;
import com.ibm.iot.connector.core.ExternalSystem;

public class Impact extends ExternalSystem {

	public Impact(){
		super.id = "impact";
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
		return super.endpointUrl;
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

	public MessageInPayload processPublishRequest(String body) {
		
		Gson gson = new Gson();
		MessageInPayload payload = gson.fromJson(body, MessageInPayload.class);
		
		return payload;
	}

	public MessageInRegistration processRegistrationRequest(String body) {
		Gson gson = new Gson();
		MessageInRegistration registration = gson.fromJson(body, MessageInRegistration.class);
		
		return registration;
	}

}
