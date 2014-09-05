package com.kopra.movieapp.content;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.kopra.movieapp.net.VolleyManager;
import com.kopra.movieapp.util.Consts;
import com.kopra.movieapp.util.Utils;

public class SearchSuggestionsProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try{
			String query = uri.getLastPathSegment().trim();
			if (query.equals(SearchManager.SUGGEST_URI_PATH_QUERY))
				return null;
			
			if(query.length() < 1)
				return null;
			
			return getSuggestions(query);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	private Cursor getSuggestions(String query) {
		VolleyManager.getInstance(getContext()).getRequestQueue().cancelAll("SearchSuggestions");
		String url = String.format(Utils.getUrlWithKey(getContext(), Consts.Api.SUGGESTIONS), Utils.encode(query));
		RequestFuture<JSONObject> future = RequestFuture.newFuture();
		JsonObjectRequest request = new JsonObjectRequest(url, null, future, future);
		request.setTag("SearchSuggestions");
		VolleyManager.getInstance(getContext()).getRequestQueue().add(request);
		try {
			JSONObject response = future.get(Consts.Config.TIMEOUT, TimeUnit.MILLISECONDS);
			return processResponse(response);
		}  catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
		} catch (TimeoutException e) {
			
		} catch (Exception e) {
			
		}
		return null;
	}
	
	private Cursor processResponse(JSONObject response) {
		String[] columns = new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, 
				SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA };
		MatrixCursor cursor = new MatrixCursor(columns);
		try {
			JSONArray movies = response.getJSONArray("movies");
			for (int index = 0; index < movies.length(); index++) {
				JSONObject movie = movies.getJSONObject(index);
				
				int id = movie.getInt("id");
				String title = movie.getString("title");
				
				String cast = null;
				if (!movie.isNull("abridged_cast")) {
					JSONArray castArray = movie.getJSONArray("abridged_cast");
					List<String> castList = new ArrayList<String>();
					for (int castIndex = 0; castIndex < castArray.length() && castIndex < 2; castIndex++) {
						JSONObject castMember = castArray.getJSONObject(castIndex);
						castList.add(castMember.getString("name"));
					}
					cast = TextUtils.join(", ", castList);
				}
				
				cursor.newRow().add(id).add(title).add(cast).add(movie.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cursor;
	}
}
