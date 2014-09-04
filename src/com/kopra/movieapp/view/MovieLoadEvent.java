package com.kopra.movieapp.view;

import org.json.JSONObject;

public class MovieLoadEvent {

	private JSONObject response;
	
	public MovieLoadEvent(JSONObject response) {
		this.response = response;
	}
	
	public JSONObject getResponse() {
		return this.response;
	}
}
