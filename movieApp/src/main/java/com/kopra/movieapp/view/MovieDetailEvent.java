package com.kopra.movieapp.view;

import org.json.JSONObject;

import com.android.volley.VolleyError;

public class MovieDetailEvent extends Event {

	public MovieDetailEvent(JSONObject response, VolleyError error, int status) {
		super(response, error, status);
	}

}
