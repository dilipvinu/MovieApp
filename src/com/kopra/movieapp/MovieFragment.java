package com.kopra.movieapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.kopra.movieapp.util.VolleyManager;
import com.kopra.movieapp.view.MovieFailEvent;
import com.kopra.movieapp.view.MovieLoadEvent;

import de.greenrobot.event.EventBus;

public class MovieFragment extends Fragment {

//	private static final String API_URL = "http://www.myapifilms.com/search?title=%s&format=JSON";
	private static final String OMDB_URL = "http://www.omdbapi.com/?t=%s";

	private ProgressBar mProgressView;
	private ScrollView mContentView;
	private NetworkImageView mPosterView;
	private TextView mTitleView;
	private TextView mRuntimeView;
	private TextView mSummaryView;
	private RatingBar mRatingView;
	private TextView mRatingTextView;
	private TextView mGenreView;
	private TextView mLanguageView;
	private TextView mReleaseDateView;

	private MenuItem mSearchMenu;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private JSONObject mMovie;
	private boolean mProgressing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		mProgressView = (ProgressBar) rootView.findViewById(R.id.progress);
		mContentView = (ScrollView) rootView.findViewById(R.id.content);
		mPosterView = (NetworkImageView) rootView.findViewById(R.id.poster);
		mPosterView.setDefaultImageResId(R.drawable.film_primary);
		mPosterView.setErrorImageResId(R.drawable.film_primary);
		mTitleView = (TextView) rootView.findViewById(R.id.title);
		mRuntimeView = (TextView) rootView.findViewById(R.id.runtime);
		mSummaryView = (TextView) rootView.findViewById(R.id.summary);
		mRatingView = (RatingBar) rootView.findViewById(R.id.rating);
		mRatingTextView = (TextView) rootView.findViewById(R.id.ratingText);
		mGenreView = (TextView) rootView.findViewById(R.id.genre);
		mLanguageView = (TextView) rootView.findViewById(R.id.language);
		mReleaseDateView = (TextView) rootView.findViewById(R.id.releaseDate);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mRequestQueue = VolleyManager.getInstance(getActivity().getApplicationContext()).getRequestQueue();
		mImageLoader = VolleyManager.getInstance(getActivity().getApplicationContext()).getImageLoader();

		if (savedInstanceState != null) {
			mProgressing = savedInstanceState.getBoolean("progress");
			mMovie = deserializeMovie(savedInstanceState.getString("movie"));
		} else {
			String json = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("movie", null);
			mMovie = deserializeMovie(json);
			
			if (mMovie == null) {
				search("The Shawshank Redemption");
			}
		}

		setupView();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);

		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		mSearchMenu = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) mSearchMenu.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("movie", mMovie != null ? mMovie.toString() : null);
		outState.putBoolean("progress", mProgressing);
		super.onSaveInstanceState(outState);
	}

	public void search(String query) {
		if (mSearchMenu != null) {
			mSearchMenu.collapseActionView();
		}
		mProgressView.setVisibility(View.VISIBLE);
		mContentView.setVisibility(View.GONE);

		JsonObjectRequest request = null;
		try {
			request = new JsonObjectRequest(String.format(OMDB_URL, URLEncoder.encode(query, "utf-8")), null, onResponse, onError);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		mRequestQueue.add(request);
		mProgressing = true;
	}

	public void onEventMainThread(MovieLoadEvent event) {
		mProgressing = false;
		mMovie = event.getResponse();
		setupView();
	}

	public void onEventMainThread(MovieFailEvent event) {
		mProgressing = false;
		mProgressView.setVisibility(View.GONE);
		Toast.makeText(getActivity(),
				"Error - " + event.getError().getMessage(), Toast.LENGTH_SHORT)
				.show();
	}

	private Response.Listener<JSONObject> onResponse = new Response.Listener<JSONObject>() {
		@Override
		public void onResponse(JSONObject response) {
			EventBus.getDefault().post(new MovieLoadEvent(response));
		}
	};

	private Response.ErrorListener onError = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			EventBus.getDefault().post(new MovieFailEvent(error));
		}
	};

	private void setupView() {
		if (!isAdded()) {
			return;
		}

		if (mProgressing) {
			mProgressView.setVisibility(View.VISIBLE);
			mContentView.setVisibility(View.GONE);
			return;
		}

		if (mMovie == null) {
			mProgressView.setVisibility(View.GONE);
			mContentView.setVisibility(View.GONE);
			return;
		}

		try {
			boolean response = mMovie.getBoolean("Response");
			if (!response) {
				Toast.makeText(getActivity(), "No result found", Toast.LENGTH_SHORT).show();
				String json = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("movie", null);
				mMovie = deserializeMovie(json);
				setupView();
				return;
			}
			
//			mPosterView.setImageUrl(mMovie.getString("urlPoster"), mImageLoader);
			mPosterView.setImageUrl(mMovie.getString("Poster"), mImageLoader);
			
//			String title = mMovie.getString("title");
			String title = mMovie.getString("Title");
//			String year = mMovie.getString("year");
			String year = mMovie.getString("Year");
			SpannableString titleSpan = new SpannableString(title + " (" + year + ")");
			titleSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.text_light)), title.length(), titleSpan.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleSpan.setSpan(new RelativeSizeSpan(0.8f), title.length(), titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTitleView.setText(titleSpan);

//			mRuntimeView.setText(mMovie.getJSONArray("runtime").getString(0));
			mRuntimeView.setText(mMovie.getString("Runtime"));
//			mRuntimeView.setCompoundDrawablesWithIntrinsicBounds(getRatedDrawable(mMovie.getString("rated")), 0, 0, 0);
			mRuntimeView.setCompoundDrawablesWithIntrinsicBounds(getRatedDrawable(mMovie.getString("Rated")), 0, 0, 0);

//			mSummaryView.setText(mMovie.getString("simplePlot"));
			mSummaryView.setText(mMovie.getString("Plot"));

//			float ratingVal = (float) mMovie.getDouble("rating");
			float ratingVal = (float) mMovie.getDouble("imdbRating");
			mRatingView.setRating(ratingVal);
			String rating = String.valueOf(ratingVal);
			SpannableString ratingSpan = new SpannableString(rating + "/10");
			ratingSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.indigo)), 0, rating.length(), 
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ratingSpan.setSpan(new RelativeSizeSpan(1.1f), 0, rating.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			mRatingTextView.setText(ratingSpan);

//			JSONArray genres = mMovie.getJSONArray("genres");
//			List<String> genreList = new ArrayList<String>();
//			for (int index = 0; index < genres.length(); index++) {
//				genreList.add(genres.getString(index));
//			}
//			mGenreView.setText(TextUtils.join(",  ", genreList));
			mGenreView.setText(mMovie.getString("Genre"));

//			JSONArray languages = mMovie.getJSONArray("languages");
//			List<String> languageList = new ArrayList<String>();
//			for (int index = 0; index < languages.length(); index++) {
//				languageList.add(languages.getString(index));
//			}
//			mLanguageView.setText(TextUtils.join(",  ", languageList));
			mLanguageView.setText(mMovie.getString("Language"));

//			int releaseDate = mMovie.getInt("releaseDate");
//			int releaseDay = releaseDate % 100;
//			releaseDate /= 100;
//			int releaseMonth = releaseDate % 100;
//			releaseDate /= 100;
//			int releaseYear = releaseDate;
//			Calendar release = new GregorianCalendar(releaseYear, releaseMonth - 1, releaseDay);
//			mReleaseDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(release.getTime()));
			mReleaseDateView.setText(mMovie.getString("Released"));

			PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
					.putString("movie", mMovie.toString()).commit();

			mProgressView.setVisibility(View.GONE);
			mContentView.setVisibility(View.VISIBLE);
		} catch (JSONException e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "Error occured", Toast.LENGTH_SHORT).show();
			String json = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("movie", null);
			mMovie = deserializeMovie(json);
			setupView();
			return;
		}
	}

	private int getRatedDrawable(String rated) {
		if ("G".equals(rated)) {
			return R.drawable.cert_us_g;
		}
		if ("PG".equals(rated)) {
			return R.drawable.cert_us_pg;
		}
		if ("PG-13".equals(rated)) {
			return R.drawable.cert_us_pg13;
		}
		if ("R".equals(rated)) {
			return R.drawable.cert_us_r;
		}
		if ("NC-17".equals(rated)) {
			return R.drawable.cert_us_nc17;
		}
		if ("X".equals(rated)) {
			return R.drawable.cert_us_x;
		}
		if ("UNRATED".equals(rated)) {
			return R.drawable.cert_us_unrated;
		}
		if ("NOT RATED".equals(rated)) {
			return 0;
		}
		if ("TV-Y".equals(rated)) {
			return R.drawable.cert_us_tvy;
		}
		if ("TV-Y7".equals(rated)) {
			return R.drawable.cert_us_tvy7;
		}
		if ("TV-G".equals(rated)) {
			return R.drawable.cert_us_tvg;
		}
		if ("TV-PG".equals(rated)) {
			return R.drawable.cert_us_tvpg;
		}
		if ("TV-14".equals(rated)) {
			return R.drawable.cert_us_tv14;
		}
		if ("TV-MA".equals(rated)) {
			return R.drawable.cert_us_tvma;
		}
		return 0;
	}

	private JSONObject deserializeMovie(String json) {
		if (json == null) {
			return null;
		}

		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			return null;
		}
	}
}
