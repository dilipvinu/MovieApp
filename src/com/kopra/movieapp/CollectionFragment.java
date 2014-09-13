package com.kopra.movieapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.kopra.movieapp.net.UrlBuilder;
import com.kopra.movieapp.net.VolleyManager;
import com.kopra.movieapp.util.Consts;
import com.kopra.movieapp.util.ErrorHandler;
import com.kopra.movieapp.util.Utils;
import com.kopra.movieapp.view.Event;
import com.kopra.movieapp.view.MovieCollectionEvent;

import de.greenrobot.event.EventBus;

public class CollectionFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "Collection";
	
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	
	private View mProgressContainer;
	private View mListContainer;
	private SwipeRefreshLayout mSwipeContainer;
	private View mList;
	private View mEmpty;
	private TextView mEmptyMessage;
	private Button mEmptyAction;
	
	private View mBoxOfficeTitle;
	private LinearLayout mBoxOfficeContainer;
	private View mInTheatersTitle;
	private LinearLayout mInTheatersContainer;
	private View mOpeningTitle;
	private LinearLayout mOpeningContainer;
	private View mUpcomingTitle;
	private LinearLayout mUpcomingContainer;
	
	private JSONObject mBoxOfficeCollection;
	private JSONObject mInTheatersCollection;
	private JSONObject mOpeningCollection;
	private JSONObject mUpcomingCollection;
	
	private boolean mRefreshing;
	private boolean mShown = true;
	private int mCount = 4;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_collection, container, false);
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
		mBoxOfficeTitle = view.findViewById(R.id.boxOfficeTitle);
		mBoxOfficeTitle.setOnClickListener(onTitleClick);
		mBoxOfficeContainer = (LinearLayout) view.findViewById(R.id.boxOffice);
		mInTheatersTitle = view.findViewById(R.id.inTheatersTitle);
		mInTheatersTitle.setOnClickListener(onTitleClick);
		mInTheatersContainer = (LinearLayout) view.findViewById(R.id.inTheaters);
		mOpeningTitle = view.findViewById(R.id.openingTitle);
		mOpeningTitle.setOnClickListener(onTitleClick);
		mOpeningContainer = (LinearLayout) view.findViewById(R.id.opening);
		mUpcomingTitle = view.findViewById(R.id.upcomingTitle);
		mUpcomingTitle.setOnClickListener(onTitleClick);
		mUpcomingContainer = (LinearLayout) view.findViewById(R.id.upcoming);
		mList = view.findViewById(android.R.id.list);
		mEmpty = view.findViewById(android.R.id.empty);
		mEmptyMessage = (TextView) view.findViewById(R.id.emptyMessage);
		mEmptyAction = (Button) view.findViewById(R.id.emptyAction);
		mEmptyAction.setOnClickListener(onClick);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mRequestQueue = VolleyManager.getInstance(getActivity()).getRequestQueue();
		mImageLoader = VolleyManager.getInstance(getActivity()).getImageLoader();
		
		if (savedInstanceState != null) {
			mRefreshing = savedInstanceState.getBoolean("refreshing");
			mShown = savedInstanceState.getBoolean("shown");
			mBoxOfficeCollection = Utils.toJson(savedInstanceState.getString("box_office"));
			mInTheatersCollection = Utils.toJson(savedInstanceState.getString("in_theaters"));
			mOpeningCollection = Utils.toJson(savedInstanceState.getString("opening"));
			mUpcomingCollection = Utils.toJson(savedInstanceState.getString("upcoming"));
			processResponse(Consts.List.BOX_OFFICE, mBoxOfficeCollection);
			processResponse(Consts.List.IN_THEATERS, mInTheatersCollection);
			processResponse(Consts.List.OPENING, mOpeningCollection);
			processResponse(Consts.List.UPCOMING, mUpcomingCollection);
		} else {
			showList(false, false);
			loadAllCollections();
		}
	}
	
	private void loadCollection(final int type) {
		String url = new UrlBuilder(getActivity())
				.setBase(Consts.Api.BASE)
				.setMethod(Utils.getApiMethod(type))
				.setLimit(Consts.Config.LIMIT_COLLECTION)
				.build();
		JsonObjectRequest request = new JsonObjectRequest(
				url, null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						EventBus.getDefault().post(new MovieCollectionEvent(response, null, Event.SUCCESS, String.valueOf(type)));
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						EventBus.getDefault().post(new MovieCollectionEvent(null, error, Event.FAILURE, String.valueOf(type)));
					}
				});
		request.setTag(TAG);
		mRequestQueue.add(request);
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
		outState.putBoolean("refreshing", mRefreshing);
		outState.putBoolean("shown", mShown);
		outState.putString("box_office", mBoxOfficeCollection != null ? mBoxOfficeCollection.toString() : null);
		outState.putString("in_theaters", mInTheatersCollection != null ? mInTheatersCollection.toString() : null);
		outState.putString("opening", mOpeningCollection != null ? mOpeningCollection.toString() : null);
		outState.putString("upcoming", mUpcomingCollection != null ? mUpcomingCollection.toString() : null);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRefresh() {
		loadAllCollections();
	}
	
	public void onEventMainThread(MovieCollectionEvent event) {
		if (event.getStatus() == Event.SUCCESS) {
			if (event.getTag().equals(String.valueOf(Consts.List.BOX_OFFICE))) {
				mBoxOfficeCollection = event.getResponse();
				processResponse(Consts.List.BOX_OFFICE, mBoxOfficeCollection);
			}
			
			if (event.getTag().equals(String.valueOf(Consts.List.IN_THEATERS))) {
				mInTheatersCollection = event.getResponse();
				processResponse(Consts.List.IN_THEATERS, mInTheatersCollection);
			}
			
			if (event.getTag().equals(String.valueOf(Consts.List.OPENING))) {
				mOpeningCollection = event.getResponse();
				processResponse(Consts.List.OPENING, mOpeningCollection);
			}
			
			if (event.getTag().equals(String.valueOf(Consts.List.UPCOMING))) {
				mUpcomingCollection = event.getResponse();
				processResponse(Consts.List.UPCOMING, mUpcomingCollection);
			}
			
			mCount--;
			if (mCount == 0) {
				mRefreshing = false;
				mSwipeContainer.setRefreshing(false);
				showList(true, true);
			}
		} else {
			mRefreshing = false;
			mSwipeContainer.setRefreshing(false);
			VolleyManager.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
			mList.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
			mEmptyMessage.setText(ErrorHandler.getMessage(event.getError()));
			showList(true, false);
		}
	}
	
	private void loadAllCollections() {
		VolleyManager.getInstance(getActivity()).getRequestQueue().cancelAll(TAG);
		mRefreshing = true;
		mCount = 4;
		loadCollection(Consts.List.BOX_OFFICE);
		loadCollection(Consts.List.IN_THEATERS);
		loadCollection(Consts.List.OPENING);
		loadCollection(Consts.List.UPCOMING);
	}
	
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
	
	private void processResponse(int type, JSONObject response) {
		if (response == null) {
			return;
		}
		
		LinearLayout container = getContainer(type);
		if (container == null) {
			return;
		}
		
		try {
			container.removeAllViews();
			int count = getResources().getInteger(R.integer.tile_column_count);
			JSONArray movies = response.getJSONArray("movies");
			for (int index = 0; index < count && index < movies.length(); index++) {
				JSONObject movie = movies.getJSONObject(index);
				String url = null;
				try {
					url = movie.getJSONObject("posters").getString("profile").replace("_tmb.jpg", "_det.jpg");
				} catch (JSONException e) {}
				final NetworkImageView poster = new NetworkImageView(getActivity());
				poster.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
				poster.setScaleType(ScaleType.FIT_CENTER);
				poster.setDefaultImageResId(R.drawable.film_primary);
				poster.setErrorImageResId(R.drawable.film_primary);
				poster.setTag(movie.toString());
				poster.setOnClickListener(onTileClick);
				container.addView(poster);
				poster.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					@Override
					public void onGlobalLayout() {
						poster.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						poster.measure(0, 0);
						LayoutParams lp = poster.getLayoutParams();
						lp.height = (int) (poster.getWidth() * 1.5f);
						poster.requestLayout();
					}
				});
				poster.setImageUrl(url, mImageLoader);
			}
		} catch (JSONException e) {}
	}
	
	private LinearLayout getContainer(int type) {
		switch (type) {
		case Consts.List.BOX_OFFICE:
			return mBoxOfficeContainer;
		case Consts.List.IN_THEATERS:
			return mInTheatersContainer;
		case Consts.List.OPENING:
			return mOpeningContainer;
		case Consts.List.UPCOMING:
			return mUpcomingContainer;
		default:
			return null;
		}
	}
	
	private OnClickListener onTitleClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Class<?> cls = null;
			switch (view.getId()) {
			case R.id.boxOfficeTitle:
				cls = BoxOfficeActivity.class;
				break;
			case R.id.inTheatersTitle:
				cls = InTheatersActivity.class;
				break;
			case R.id.openingTitle:
				cls = OpeningMoviesActivity.class;
				break;
			case R.id.upcomingTitle:
				cls = UpcomingMoviesActivity.class;
				break;
			default:
				return;
			}
			Intent intent = new Intent(getActivity(), cls);
			startActivity(intent);
		}
	};
	
	private OnClickListener onTileClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			String movie = (String) view.getTag();
			Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
			detailIntent.putExtra("movie", movie);
			startActivity(detailIntent);
		}
	};
	
	private OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			showList(false, false);
			mList.setVisibility(View.VISIBLE);
			mEmpty.setVisibility(View.GONE);
			loadAllCollections();
		}
	};
}
