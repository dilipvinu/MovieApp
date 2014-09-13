package com.kopra.movieapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kopra.movieapp.util.Rated;
import com.kopra.movieapp.util.Utils;
import com.kopra.movieapp.view.Event;
import com.kopra.movieapp.view.MovieDetailEvent;

import de.greenrobot.event.EventBus;

public class MovieDetailFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

	private View mProgressContainer;
	private View mListContainer;
	private SwipeRefreshLayout mSwipeContainer;
	private View mContent;
	private View mEmpty;
	private TextView mEmptyMessage;
	private Button mEmptyAction;
	
	private NetworkImageView mPosterView;
	private TextView mTitleView;
	private TextView mRuntimeView;
	private TextView mSummaryView;
	private RatingBar mRatingView;
	private TextView mRatingTextView;
	private TextView mGenreView;
	private TextView mLanguageView;
	private TextView mReleaseDateView;
	
	private ShareActionProvider mShareProvider;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private JSONObject mMovie;
	private boolean mRefreshing;
	private boolean mShown = true;
	
	public static MovieDetailFragment newInstance(String movie) {
		MovieDetailFragment fragment = new MovieDetailFragment();
		Bundle args = new Bundle();
		args.putString("movie", movie);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_movie, container, false);
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
		mContent = view.findViewById(R.id.content);
		mPosterView = (NetworkImageView) view.findViewById(R.id.poster);
		mPosterView.setDefaultImageResId(R.drawable.film_primary);
		mPosterView.setErrorImageResId(R.drawable.film_primary);
		mTitleView = (TextView) view.findViewById(R.id.title);
		mRuntimeView = (TextView) view.findViewById(R.id.runtime);
		mSummaryView = (TextView) view.findViewById(R.id.summary);
		mRatingView = (RatingBar) view.findViewById(R.id.rating);
		mRatingTextView = (TextView) view.findViewById(R.id.ratingText);
		mGenreView = (TextView) view.findViewById(R.id.genre);
		mLanguageView = (TextView) view.findViewById(R.id.language);
		mReleaseDateView = (TextView) view.findViewById(R.id.releaseDate);
		mEmpty = view.findViewById(android.R.id.empty);
		mEmptyMessage = (TextView) view.findViewById(R.id.emptyMessage);
		mEmptyAction = (Button) view.findViewById(R.id.emptyAction);
		mEmptyAction.setOnClickListener(onClick);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getActivity().setTitle(null);

		mRequestQueue = VolleyManager.getInstance(getActivity().getApplicationContext()).getRequestQueue();
		mImageLoader = VolleyManager.getInstance(getActivity().getApplicationContext()).getImageLoader();

		if (savedInstanceState != null) {
			mShown = savedInstanceState.getBoolean("shown");
			mRefreshing = savedInstanceState.getBoolean("refreshing");
			mMovie = Utils.toJson(savedInstanceState.getString("movie"));
		} else {
			showContent(false, false);
			loadMovie();
		}

		setupView();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.details, menu);
		
		MenuItem mnuShare = menu.findItem(R.id.action_share);
		mShareProvider = (ShareActionProvider) mnuShare.getActionProvider();
		setShareIntent();
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
		outState.putString("movie", mMovie != null ? mMovie.toString() : null);
		outState.putBoolean("refreshing", mRefreshing);
		outState.putBoolean("shown", mShown);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onRefresh() {
		loadMovie();
	}

	public void onEventMainThread(MovieDetailEvent event) {
		mRefreshing = false;
		mSwipeContainer.setRefreshing(false);
		if (event.getStatus() == Event.SUCCESS) {
			mMovie = event.getResponse();
			setupView();
		} else {
			mContent.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
			mEmptyMessage.setText(ErrorHandler.getMessage(event.getError()));
			showContent(true, false);
		}
	}

	private Response.Listener<JSONObject> onResponse = new Response.Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			EventBus.getDefault().post(new MovieDetailEvent(response, null, Event.SUCCESS));
		}
	};

	private Response.ErrorListener onError = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			EventBus.getDefault().post(new MovieDetailEvent(null, error, Event.FAILURE));
		}
	};
	
	private OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			showContent(false, false);
			mContent.setVisibility(View.VISIBLE);
			mEmpty.setVisibility(View.GONE);
			loadMovie();
		}
	};
	
	private void loadMovie() {
		JSONObject movie = Utils.toJson(getArguments().getString("movie"));
		if (movie == null) {
			getActivity().finish();
			return;
		}
		
		String method = null;
		try {
			if (!movie.isNull("alternate_ids") && !movie.getJSONObject("alternate_ids").isNull("imdb")) {
				String id = movie.getJSONObject("alternate_ids").getString("imdb");
				method = String.format(Consts.Api.MOVIE_DETAIL_OMDB, id);
			} else {
				Toast.makeText(getActivity(), R.string.no_imdb_entry, Toast.LENGTH_SHORT).show();
				getActivity().finish();
				return;
			}
		} catch (JSONException e) {}

		String url = new UrlBuilder(getActivity())
				.setBase(Consts.Api.BASE_OMDB)
				.setMethod(method)
				.build();
		JsonObjectRequest request = new JsonObjectRequest(url, null, onResponse, onError);
		
		mRequestQueue.add(request);
		mRefreshing = true;
	}
	
	private void showContent(boolean show, boolean animate) {
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

	private void setupView() {
		if (!isAdded()) {
			return;
		}

		if (mRefreshing) {
			showContent(false, false);
			return;
		}

		if (mMovie == null) {
			mContent.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
			mEmptyMessage.setText(ErrorHandler.getMessage(null));
			showContent(true, false);
			return;
		}

		try {			
//			mPosterView.setImageUrl(mMovie.getJSONObject("posters").getString("detailed").replace("_tmb.jpg", "_det.jpg"), mImageLoader);
//			
//			String title = mMovie.getString("title");
//			String year = mMovie.getString("year");
//			SpannableString titleSpan = new SpannableString(title + " (" + year + ")");
//			titleSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_light)), title.length(), titleSpan.length(),
//					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			titleSpan.setSpan(new RelativeSizeSpan(0.8f), title.length(), titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			mTitleView.setText(titleSpan);
//
//			mRuntimeView.setText(mMovie.getString("runtime") + " min");
//			mRuntimeView.setCompoundDrawablesWithIntrinsicBounds(Rated.getDrawable(mMovie.getString("mpaa_rating")), 0, 0, 0);
//
//			mSummaryView.setText(mMovie.getString("synopsis"));
//
//			mGenreView.setText(mMovie.getJSONArray("genres").join(", ").replace("\"", ""));
//
//			try {
//				String releaseDate = mMovie.getJSONObject("release_dates").getString("theater");
//				mReleaseDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate)));
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
			
			mPosterView.setImageUrl(mMovie.getString("Poster"), mImageLoader);
			
			String title = mMovie.getString("Title");
			String year = mMovie.getString("Year");
			SpannableString titleSpan = new SpannableString(title + " (" + year + ")");
			titleSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_secondary)), title.length(), titleSpan.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleSpan.setSpan(new RelativeSizeSpan(0.8f), title.length(), titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTitleView.setText(titleSpan);

			mRuntimeView.setText(mMovie.getString("Runtime"));
			mRuntimeView.setCompoundDrawablesWithIntrinsicBounds(Rated.getDrawable(mMovie.getString("Rated")), 0, 0, 0);

			mSummaryView.setText(mMovie.getString("Plot"));

			String rating = mMovie.getString("imdbRating");
			try {
				float ratingVal = (float) Double.parseDouble(rating);
				mRatingView.setRating(ratingVal);
				SpannableString ratingSpan = new SpannableString(rating + "/10");
				ratingSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.indigo)), 0, rating.length(), 
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				ratingSpan.setSpan(new RelativeSizeSpan(1.1f), 0, rating.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mRatingTextView.setText(ratingSpan);
			} catch (NumberFormatException e) {
				mRatingTextView.setText(rating);
			}

			mGenreView.setText(mMovie.getString("Genre"));

			mLanguageView.setText(mMovie.getString("Language"));

			mReleaseDateView.setText(mMovie.getString("Released"));
			
			setShareIntent();

			showContent(true, true);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return;
		}
	}

	private void setShareIntent() {
		if (mShareProvider == null) {
			return;
		}
		
		try {
			JSONObject movie = Utils.toJson(getArguments().getString("movie"));
			if (!movie.isNull("alternate_ids") && !movie.getJSONObject("alternate_ids").isNull("imdb")) {
				String id = movie.getJSONObject("alternate_ids").getString("imdb");
				String url = getString(R.string.share_url, id);
				String caption = movie.getString("title") + " (" + movie.getString("year") + ")";
				
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, caption);
				shareIntent.putExtra(Intent.EXTRA_TEXT, url);
				
				mShareProvider.setShareIntent(shareIntent);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
