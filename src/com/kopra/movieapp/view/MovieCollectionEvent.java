package com.kopra.movieapp.view;

import org.json.JSONObject;

import com.android.volley.VolleyError;

public class MovieCollectionEvent extends Event {

	public MovieCollectionEvent(JSONObject response, VolleyError error, int status) {
		this(response, error, status, null);
	}
	
	public MovieCollectionEvent(JSONObject response, VolleyError error, int status, String tag) {
		super(response, error, status, tag);
	}

}
