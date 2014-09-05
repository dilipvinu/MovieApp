package com.kopra.movieapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kopra.movieapp.net.VolleyManager;
import com.kopra.movieapp.util.Consts;
import com.kopra.movieapp.util.Utils;
import com.kopra.movieapp.view.Event;
import com.kopra.movieapp.view.SearchListEvent;
import com.kopra.movieapp.widget.MovieAdapter;

import de.greenrobot.event.EventBus;

public class MovieListFragment extends ListFragment {

	private RequestQueue mRequestQueue;
	private JSONObject mResults;
	
	public static MovieListFragment newInstance(int type, String query) {
		MovieListFragment fragment = new MovieListFragment();
		Bundle args = new Bundle();
		args.putInt("type", type);
		args.putString("query", query);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void search(String query) {
		JsonObjectRequest request = new JsonObjectRequest(
				String.format(Utils.getUrlWithKey(getActivity(), getApi()), Utils.encode(query)), 
				null, onResponse, onError);
		mRequestQueue.add(request);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mRequestQueue = VolleyManager.getInstance(getActivity()).getRequestQueue();
		
		if (savedInstanceState != null) {
			mResults = Utils.toJson(savedInstanceState.getString("results"));
			processResponse(mResults);
		} else {
			search(getArguments().getString("query"));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("results", mResults != null ? mResults.toString() : null);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		JSONObject movie = (JSONObject) parent.getItemAtPosition(position);
		Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
		detailIntent.putExtra("movie", movie.toString());
		startActivity(detailIntent);
	}
	
	public void onEventMainThread(SearchListEvent event) {
		if (event.getStatus() == Event.SUCCESS) {
			mResults = event.getResponse();
			processResponse(mResults);
		}
	}
	
	private Response.Listener<JSONObject> onResponse = new Response.Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			EventBus.getDefault().post(new SearchListEvent(response, null, Event.SUCCESS));
		}
	};

	private Response.ErrorListener onError = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			EventBus.getDefault().post(new SearchListEvent(null, error, Event.FAILURE));
		}
	};	
	
	private void processResponse(JSONObject response) {
		if (response == null)
			return;
		
		try {
			JSONArray movies = response.getJSONArray("movies");
			List<JSONObject> list = new ArrayList<JSONObject>();
			for (int index = 0; index < movies.length(); index++) {
				list.add(movies.getJSONObject(index));
			}
			setListAdapter(new MovieAdapter(getActivity(), list));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private String getApi() {
		int type = getArguments().getInt("type");
		switch (type) {
		case Consts.List.SEARCH:
			return Consts.Api.SEARCH;
		case Consts.List.BOX_OFFICE:
			return Consts.Api.BOX_OFFICE;
		default:
			return null;
		}
	}
}
