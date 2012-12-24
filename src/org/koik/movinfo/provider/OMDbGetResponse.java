package org.koik.movinfo.provider;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class OMDbGetResponse extends OMDbAbstractResponse{
	public OMDbGetResponse(String response) throws ParseException {
		super(response);
		if (success())
			parse(getResponseJson());
	}
	
	protected void parse(JSONObject jsonResp) {
		result = new OMDbResult(jsonResp);
	}
	
	OMDbResult result = null;
	public OMDbResult getResult() {
		return result;
	}
}
