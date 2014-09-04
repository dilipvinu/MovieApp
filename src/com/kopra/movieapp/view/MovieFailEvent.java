package com.kopra.movieapp.view;

import com.android.volley.VolleyError;

public class MovieFailEvent {

	private VolleyError error;
	
	public MovieFailEvent(VolleyError error) {
		this.error = error;
	}
	
	public VolleyError getError() {
		return this.error;
	}
}
