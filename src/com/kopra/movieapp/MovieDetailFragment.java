package com.kopra.movieapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
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
	
	private TextView mImdbRatingView;
	private ImageView mRottenLogoView;
	private TextView mRottenRatingView;
	private ImageView mFlixsterLogoView;
	private TextView mFlixsterRatingView;
	private TextView mMetaRatingView;
	
	private TextView mCastView;
	private TextView mGenreView;
	private TextView mLanguageView;
	private TextView mReleaseDateView;
	
	private ShareActionProvider mShareProvider;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private JSONObject mIMDbMovie;
	private boolean mRefreshing;
	private boolean mShown = true;
	private boolean mPlotDialogShown;
	
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
		mPosterView.setOnClickListener(onPosterClick);
		mTitleView = (TextView) view.findViewById(R.id.title);
		mRuntimeView = (TextView) view.findViewById(R.id.runtime);
		mSummaryView = (TextView) view.findViewById(R.id.summary);
		mSummaryView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int maxLines = (int) mSummaryView.getHeight() / mSummaryView.getLineHeight();
				mSummaryView.setMaxLines(maxLines);
				
				Layout layout = mSummaryView.getLayout();
				if (layout != null) {
					int lines = layout.getLineCount();
					if (lines > 0) {
						if (layout.getEllipsisCount(lines - 1) > 0) {
							mSummaryView.setOnClickListener(onSummaryClick);
						} else {
							mSummaryView.setOnClickListener(null);
						}
					}
				}
			}
		});
		
		mImdbRatingView = (TextView) view.findViewById(R.id.imdbRating);
		mRottenLogoView = (ImageView) view.findViewById(R.id.rottenLogo);
		mRottenRatingView = (TextView) view.findViewById(R.id.rottenRating);
		mFlixsterLogoView = (ImageView) view.findViewById(R.id.flixsterLogo);
		mFlixsterRatingView = (TextView) view.findViewById(R.id.flixsterRating);
		mMetaRatingView = (TextView) view.findViewById(R.id.metacriticRating);
		
		mCastView = (TextView) view.findViewById(R.id.cast);
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
			mIMDbMovie = Utils.toJson(savedInstanceState.getString("movie"));
			mPlotDialogShown = savedInstanceState.getBoolean("plot_dialog_shown");
			if (mPlotDialogShown) {
				showPlotSummry();
			}
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
		outState.putString("movie", mIMDbMovie != null ? mIMDbMovie.toString() : null);
		outState.putBoolean("refreshing", mRefreshing);
		outState.putBoolean("shown", mShown);
		outState.putBoolean("plot_dialog_shown", mPlotDialogShown);
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
			mIMDbMovie = event.getResponse();
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
	
	private OnClickListener onSummaryClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			showPlotSummry();
		}
	};
	
	private OnClickListener onPosterClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			try {
				JSONObject movie = Utils.toJson(getArguments().getString("movie"));
				String url = movie.getJSONObject("posters").getString("original").replace("_tmb.jpg", "_ori.jpg");
				
				int[] screenLocation = new int[2];
				view.getLocationOnScreen(screenLocation);
		        Intent intent = new Intent(getActivity(), ImageActivity.class);
		        intent
		        	.putExtra("bitmap", ((BitmapDrawable) mPosterView.getDrawable()).getBitmap())
		        	.putExtra("left", screenLocation[0])
		        	.putExtra("top", screenLocation[1])
		        	.putExtra("width", view.getWidth())
		        	.putExtra("height", view.getHeight())
		        	.putExtra("url", url);
		        startActivity(intent);
		        getActivity().overridePendingTransition(0, 0);
			} catch (JSONException e) {}
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

		if (mIMDbMovie == null) {
			mContent.setVisibility(View.GONE);
			mEmpty.setVisibility(View.VISIBLE);
			mEmptyMessage.setText(ErrorHandler.getMessage(null));
			showContent(true, false);
			return;
		}

		try {
			JSONObject movie = Utils.toJson(getArguments().getString("movie"));
			
			mPosterView.setImageUrl(movie.getJSONObject("posters").getString("detailed").replace("_tmb.jpg", "_det.jpg"), mImageLoader);
			
			String title = movie.getString("title");
			String year = movie.getString("year");
			SpannableString titleSpan = new SpannableString(title + " (" + year + ")");
			titleSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_secondary)), title.length(), titleSpan.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleSpan.setSpan(new RelativeSizeSpan(0.8f), title.length(), titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTitleView.setText(titleSpan);

			mRuntimeView.setText(Utils.formatTime(getActivity(), movie.optInt("runtime")));
			mRuntimeView.setCompoundDrawablesWithIntrinsicBounds(Rated.getDrawable(mIMDbMovie.getString("Rated")), 0, 0, 0);
			
			mSummaryView.setText(!mIMDbMovie.isNull("Plot") ? mIMDbMovie.getString("Plot") : getString(R.string.na));
			
			float imdbScore = (!mIMDbMovie.isNull("imdbRating") ? (float) mIMDbMovie.optDouble("imdbRating", 0) : 0);
			int criticScore = movie.getJSONObject("ratings").optInt("critics_score");
			int audienceScore = movie.getJSONObject("ratings").optInt("audience_score");
			int metaScore = (!mIMDbMovie.isNull("Metascore") ? mIMDbMovie.optInt("Metascore") : 0);
			
			mImdbRatingView.setText(imdbScore > 0 ? String.valueOf(imdbScore) : getString(R.string.na));
			
			if (criticScore < 60) {
				mRottenLogoView.setImageResource(R.drawable.rotten);
			}
			mRottenRatingView.setText(criticScore > 0 ? String.valueOf(criticScore) + "%" : getString(R.string.na));
			
			if (audienceScore < 60) {
				mFlixsterLogoView.setImageResource(R.drawable.spilt);
			}
			mFlixsterRatingView.setText(audienceScore > 0 ? String.valueOf(audienceScore) + "%" : getString(R.string.na));
			
			mMetaRatingView.setText(metaScore > 0 ? String.valueOf(metaScore) : getString(R.string.na));

			JSONArray cast = movie.getJSONArray("abridged_cast");
			List<String> castList = new ArrayList<String>();
			for (int index = 0; index < cast.length(); index++) {
				JSONObject castMember = cast.getJSONObject(index);
				castList.add(castMember.getString("name"));
			}
			mCastView.setText(TextUtils.join(", ", castList));

			mGenreView.setText(!mIMDbMovie.isNull("Genre") ? mIMDbMovie.getString("Genre") : getString(R.string.na));

			mLanguageView.setText(!mIMDbMovie.isNull("Language") ? mIMDbMovie.getString("Language") : getString(R.string.na));

			try {
				String releaseDate = movie.getJSONObject("release_dates").getString("theater");
				mReleaseDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(releaseDate)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			setShareIntent();

			showContent(true, true);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return;
		}
	}
	
	private void showPlotSummry() {
		try {
			AlertDialog alert = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.plot_summary)
				.setMessage(mIMDbMovie.getString("Plot"))
				.create();
			alert.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mPlotDialogShown = false;
				}
			});
			alert.show();
			mPlotDialogShown = true;
		} catch (JSONException e) {
			e.printStackTrace();
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
