package com.kopra.movieapp.util;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.kopra.movieapp.R;

public class ErrorHandler {

	public static int getMessage(VolleyError error) {
		if (error == null) {
			return R.string.error_unknown;
		}
		
		if (error instanceof NetworkError) {
			return R.string.error_connection;
		}
		
		if (error instanceof NoConnectionError) {
			return R.string.error_connection;
		}
		
		if (error instanceof TimeoutError) {
			return R.string.error_connection;
		}
		
		return R.string.error_unknown;
	}
}
