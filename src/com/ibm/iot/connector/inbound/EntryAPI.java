package com.ibm.iot.connector.inbound;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.ibm.iot.connector.core.ExternalSystem;
import com.ibm.iot.connector.domain.Impact;
import com.ibm.iot.connector.domain.MessageInPayload;
import com.ibm.iot.connector.domain.WatsonIoT;
import com.ibm.iot.connector.utils.HTTPClient;

/**
 * Servlet implementation class EntryAPI
 */
@Path("/")
public class EntryAPI{
	private static String CLASS_NAME = EntryAPI.class.getName();
	private static final Logger LOGGER = Logger.getLogger(EntryAPI.class.getName());
	
	public String a = "init";
	
	
	@Path("/callback")
	@PUT
	@Produces("application/json")
	public Response setCallback(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
			@Context final HttpServletResponse response, @PathParam("orgId") String orgId,
			@PathParam("connectorId") String connectorId, @PathParam("connectorVersion") String connectorVersion,
			String body){
		final String METHOD_NAME = "HttpClientBuilder"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {});
		
		
		LOGGER.logp(Level.FINER, CLASS_NAME, METHOD_NAME, "instantiated.");
		
		
		HTTPClient httpClient = new HTTPClient();
		ExternalSystem impact = new Impact();
		HttpPut httpPut = new HttpPut(impact.getEndPointUrl());
		
		HttpEntity entity = null;
		entity = new StringEntity(impact.processCallbackRequest(body), ContentType.APPLICATION_JSON);
		httpPut.setEntity(entity);

		/*
		Map<String, String> headers = httpClient.extractFromRequest(httpHeaders.getRequestHeaders());
		impact.setHeader(headers);

		// Transfer headers from connector instance to Http request
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpPut.setHeader(entry.getKey(), entry.getValue());
		}
		*/
		
		HttpResponse extSysResponse = httpClient.submitRequest(httpPut);
		try {
			String strExtSysResponse = httpClient.extractContentfrom(extSysResponse);
			impact.processCallbackResponse(strExtSysResponse);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return Response.status(403).entity("{}").build();
	}
	
	@Path("/publish")
	@POST
	@Produces("application/json")
	public Response publish(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
			@Context final HttpServletResponse response, @PathParam("orgId") String orgId,
			@PathParam("connectorId") String connectorId, @PathParam("connectorVersion") String connectorVersion,
			String body){
		final String METHOD_NAME = "publish"; //$NON-NLS-1$
		LOGGER.entering(CLASS_NAME, METHOD_NAME, new Object[] {});
		
		this.a = "publish";
		
		if(body.trim().equals("{}") || body==null || body.trim().equals("")){
			LOGGER.exiting(CLASS_NAME, METHOD_NAME);
			return Response.status(Status.OK).entity("{}").build();
		}
		
		
		WatsonIoT iotP = new WatsonIoT();
		iotP.orgId = "2lk5v0";
		iotP.apiKey = "a-2lk5v0-9jrlhoeswb";
		iotP.apiToken = "51_HKMbh&JU-X1J9Dj";
				
		Impact impact = new Impact();
		
		//Process inbound payload - convert body JSON to object
		MessageInPayload payload = impact.processPublishRequest(body);
		
		//Create devices
		if(payload.registrations!=null && payload.registrations.length>0){
			LOGGER.logp(Level.FINER, CLASS_NAME, METHOD_NAME, "Payload contains registration entries");
			iotP.createDevices(impact.getId(), payload.registrations);
		}
		
		//Publish all notifications
		if(payload.reports!=null && payload.reports.length>0){
			LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Payload contains observation entries");
			iotP.publishEvents(impact.getId(), payload.reports);			
		}
		
		LOGGER.exiting(CLASS_NAME, METHOD_NAME);
		return Response.status(Status.OK).entity("{}").build();
	}
	
	@Path("/config")
	@GET
	@Produces("application/json")
	public Response getConfiguration(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
			@Context final HttpServletResponse response, @PathParam("orgId") String orgId,
			@PathParam("connectorId") String connectorId, @PathParam("connectorVersion") String connectorVersion,
			String body){
		
		System.out.println("a:"+this.a);
		
		return Response.status(Status.OK).entity("{}").build();
		
	}
	
	@Path("/config")
	@POST
	@Produces("application/json")
	public Response setConfiguration(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders,
			@Context final HttpServletResponse response, @PathParam("orgId") String orgId,
			@PathParam("connectorId") String connectorId, @PathParam("connectorVersion") String connectorVersion,
			String body){
		
		return Response.status(Status.OK).entity("{}").build();
		
	}
	
	
}
