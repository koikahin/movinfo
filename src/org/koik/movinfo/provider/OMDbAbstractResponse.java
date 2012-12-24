package org.koik.movinfo.provider;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class OMDbAbstractResponse {
	final protected JSONObject responseJson;
	protected JSONObject getResponseJson() {
		return responseJson;
	}
	private boolean success = true;
	public boolean success() {
		return success;
	}
	
	private String failureMessage = null;
	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}
	public String getFailureMessage() {
		return failureMessage;
	}
	
	protected void setFailure(String msg) {
		success = false;
		setFailureMessage(msg);
	}
	
	public OMDbAbstractResponse(String response) throws ParseException {
		responseJson = (JSONObject) new JSONParser().parse(response);
		if (responseJson.containsKey("Error")) {
			setFailure((String)responseJson.get("Error"));
		} 
	}
}
