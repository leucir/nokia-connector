package com.ibm.iot.connector.domain;

import com.google.gson.annotations.SerializedName;

public class MessageInRegistration {

	@SerializedName("reports")
	public RegistrationReports[] reports;
	
	@SerializedName("registrations")
	public RegistrationRegistrations[] registrations;

	@SerializedName("deregistrations")
	public RegistrationDeregistrations[] deregistrations;

	@SerializedName("updates")
	public RegistrationUpdates[] updates;

	@SerializedName("expirations")
	public RegistrationExpirations[] expirations;
	
	@SerializedName("responses")
	public RegistrationResponses[] responses;
}
