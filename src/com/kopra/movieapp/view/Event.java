package com.kopra.movieapp.view;

import org.json.JSONObject;

import com.android.volley.VolleyError;

public class Event {

	public static final int SUCCESS = 0;
	public static final int FAILURE = -1;
	
	private JSONObject response;
	private VolleyError error;
	private int status;
	
	public Event(JSONObject response, VolleyError error, int status) {
		this.response = response;
		this.error = error;
		this.status = status;
	}
	
	public JSONObject getResponse() {
		return this.response;
	}
	
	public VolleyError getError() {
		return this.error;
	}
	
	public int getStatus() {
		return this.status;
	}
}
