package com.kopra.movieapp;

import com.kopra.movieapp.util.Consts;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class SearchActivity extends BaseActivity {

	private static final String TAG = "MovieListFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			handleIntent(getIntent());
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Intent detailIntent = new Intent(this, MovieDetailActivity.class);
			String movie = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
			detailIntent.putExtra("movie", movie);
			startActivity(detailIntent);
			finish();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			MovieListFragment fragment = (MovieListFragment) getFragmentManager().findFragmentByTag(TAG);
			if (fragment == null) {
				getFragmentManager().beginTransaction()
					.add(android.R.id.content, MovieListFragment.newInstance(Consts.List.SEARCH, query), TAG).commit();
			} else {
				fragment.search(query);
			}
		}
	}
}
