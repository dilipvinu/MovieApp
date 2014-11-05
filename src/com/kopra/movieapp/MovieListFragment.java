package com.kopra.movieapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kopra.movieapp.net.UrlBuilder;
import com.kopra.movieapp.net.VolleyManager;
import com.kopra.movieapp.util.Consts;
import com.kopra.movieapp.util.ErrorHandler;
import com.kopra.movieapp.util.Utils;
import com.kopra.movieapp.view.Event;
import com.kopra.movieapp.view.MovieListEvent;
import com.kopra.movieapp.widget.MovieAdapter;

import de.greenrobot.event.EventBus;

public class MovieListFragment extends BaseListFragment implements SwipeRefreshLayout.OnRefreshListener, TypeFragment {

	private RequestQueue mRequestQueue;
	private JSONObject mResults;
	
	private View mProgressContainer;
	private View mListContainer;
	private SwipeRefreshLayout mSwipeContainer;
	private View mFooterView;
	private TextView mEmptyMessage;
	private Button mEmptyAction;
	
	private MovieAdapter mAdapter;
	private List<JSONObject> mList = new ArrayList<JSONObject>();
	
	private boolean mRefreshing;
	private boolean mLoading;
	private boolean mShown = true;
	private boolean mHasRequestedMore;
	private int mPage = 1;
	private int mTotal;
	private int mType;
	private String mTitle;
	
	public static MovieListFragment newInstance(int type, String title, String query, boolean paged) {
		MovieListFragment fragment = new MovieListFragment();
		Bundle args = new Bundle();
		args.putInt("type", type);
		args.putString("title", title);
		args.putString("query", query);
		args.putBoolean("paged", paged);
		fragment.setArguments(args);
		return fragment;
	}
	
	public void search(String query) {
		int type = getArguments().getInt("type");
		search(type, query);
	}
	
	public void search(int type, String query) {
		String url = new UrlBuilder(getActivity())
				.setBase(Consts.Api.BASE)
				.setMethod(String.format(Utils.getApiMethod(type), Utils.encode(query)))
				.setPage(mPage)
				.setLimit(Consts.Config.LIMIT_SEARCH)
				.build();
		JsonObjectRequest request = new JsonObjectRequest(
				url, null, onResponse, onError);
		mRequestQueue.add(request);
		mLoading = true;
	}
	
	@Override
	public int getType() {
		return mType;
	}
	
	@Override
	public String getTitle() {
		return mTitle;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getArguments().getInt("type");
		mTitle = getArguments().getString("title");
		if (savedInstanceState != null) {
			mRefreshing = savedInstanceState.getBoolean("refreshing");
			mLoading = savedInstanceState.getBoolean("loading");
			mShown = savedInstanceState.getBoolean("shown");
			mResults = Utils.toJson(savedInstanceState.getString("results"));
			mHasRequestedMore = savedInstanceState.getBoolean("requested_more");
			mPage = savedInstanceState.getInt("page");
			mTotal = savedInstanceState.getInt("total");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mProgressContainer = view.findViewById(R.id.progressContainer);
		mListContainer = view.findViewById(R.id.listContainer);
		mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
		mSwipeContainer.setOnRefreshListener(this);
		mSwipeContainer.setColorSchemeResources(
				android.R.color.holo_blue_bright, 
				android.R.color.holo_green_light, 
				android.R.color.holo_orange_light, 
				android.R.color.holo_red_light);
		mEmptyMessage = (TextView) view.findViewById(R.id.emptyMessage);
		mEmptyAction = (Button) view.findViewById(R.id.emptyAction);
		mEmptyAction.setOnClickListener(onClick);
		
		mFooterView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pending, (ViewGroup) getView(), false);
		addFooterView(mFooterView);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mRequestQueue = VolleyManager.getInstance(getActivity()).getRequestQueue();
		
		if (mAdapter == null) {
			if (savedInstanceState != null) {
				mRefreshing = savedInstanceState.getBoolean("refreshing");
				mLoading = savedInstanceState.getBoolean("loading");
				mShown = savedInstanceState.getBoolean("shown");
				mResults = Utils.toJson(savedInstanceState.getString("results"));
				mHasRequestedMore = savedInstanceState.getBoolean("requested_more");
				mPage = savedInstanceState.getInt("page");
				mTotal = savedInstanceState.getInt("total");
				processResponse(mResults);
			} else {
				showList(false, false);
				search(getArguments().getString("query"));
			}
		} else {
			setListAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
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
		super.onSaveInstanceState(outState);
		saveInstance(outState);
	}
	
	@Override
	public void onListItemClick(AbsListView parent, View view, int position, long id) {
		JSONObject movie = (JSONObject) parent.getItemAtPosition(position);
		Fragment fragment = MovieDetailFragment.newInstance(movie.toString());
		((MainActivity) getActivity()).addFragment(-1, fragment, false);
	}
	
	@Override
	public void onListScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (!mRefreshing && !mHasRequestedMore) {
			int lastInScreen = firstVisibleItem + visibleItemCount;
			if (lastInScreen >= totalItemCount && totalItemCount < mTotal) {
				mHasRequestedMore = true;
				mPage++;
				search(getArguments().getString("query"));
			}
		}
	}
	
	@Override
	public void onRefresh() {
		mRefreshing = true;
		mPage = 1;
		search(getArguments().getString("query"));
	}
	
	public void onEventMainThread(MovieListEvent event) {
		mLoading = false;
		mSwipeContainer.setRefreshing(false);
		
		if (event.getStatus() == Event.SUCCESS) {
			if (mRefreshing) {
				clearResults();
				removeFooterView(mFooterView);
				addFooterView(mFooterView);
			}
			appendResults(event.getResponse());
			processResponse(event.getResponse());
		} else {
			mEmptyMessage.setText(ErrorHandler.getMessage(event.getError()));
			showList(true, false);
		}
	}
	
	private Response.Listener<JSONObject> onResponse = new Response.Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			EventBus.getDefault().post(new MovieListEvent(response, null, Event.SUCCESS));
		}
	};

	private Response.ErrorListener onError = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			EventBus.getDefault().post(new MovieListEvent(null, error, Event.FAILURE));
		}
	};
	
	private OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			showList(false, false);
			search(getArguments().getString("query"));
		}
	};
	
	private void showList(boolean show, boolean animate) {
		if (mShown == show) {
			return;
		}
		
		if (show) {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
			} else {
				mProgressContainer.clearAnimation();
				mListContainer.clearAnimation();
			}
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
			} else {
				mProgressContainer.clearAnimation();
				mListContainer.clearAnimation();
			}
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
		}
		mShown = show;
	}
	
	private void processResponse(JSONObject response) {
		if (mLoading) {
			showList(false, false);
			return;
		}
		
		if (response == null) {
			mEmptyMessage.setText(ErrorHandler.getMessage(null));
			showList(true, false);
			return;
		}
		
		try {
			mTotal = response.optInt("total");
			if (mRefreshing) {
				mRefreshing = false;
				mList.clear();
			}
			JSONArray movies = response.getJSONArray("movies");
			for (int index = 0; index < movies.length(); index++) {
				mList.add(movies.getJSONObject(index));
			}
			if (mAdapter == null) {
				mAdapter = new MovieAdapter(getActivity(), mList);
				setListAdapter(mAdapter);
				showList(true, true);
			}
			mAdapter.notifyDataSetChanged();
			mHasRequestedMore = false;
			
			if (mList.size() >= mTotal || mPage >= Consts.Config.MAX_PAGE) {
				removeFooterView(mFooterView);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void appendResults(JSONObject response) {
		try {
			if (mResults == null) {
				mResults = new JSONObject();
				mResults.put("movies", new JSONArray());
			}
			
			int total = response.optInt("total");
			mResults.put("total", total);
			
			JSONArray movies = response.getJSONArray("movies");
			for (int index = 0; index < movies.length(); index++) {
				mResults.getJSONArray("movies").put(movies.getJSONObject(index));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void clearResults() {
		try {
			if (mResults == null) {
				mResults = new JSONObject();
			}
			mResults.put("movies", new JSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void saveInstance(Bundle state) {
		state.putBoolean("refreshing", mRefreshing);
		state.putBoolean("loading", mLoading);
		state.putBoolean("shown", mShown);
		state.putString("results", mResults != null ? mResults.toString() : null);
		state.putBoolean("requested_more", mHasRequestedMore);
		state.putInt("page", mPage);
		state.putInt("total", mTotal);
	}
	
}
