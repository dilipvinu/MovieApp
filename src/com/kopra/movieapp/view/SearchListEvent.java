package com.kopra.movieapp.view;

import org.json.JSONObject;

import com.android.volley.VolleyError;

public class SearchListEvent extends Event {

	public SearchListEvent(JSONObject response, VolleyError error, int status) {
		super(response, error, status);
	}

}
