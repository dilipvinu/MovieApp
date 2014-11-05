package com.kopra.movieapp;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class SearchActivity extends ActionBarActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
			Intent resultIntent = new Intent(this, SearchResultActivity.class);
			String query = intent.getStringExtra(SearchManager.QUERY);
			resultIntent.putExtra("query", query);
			startActivity(resultIntent);
			finish();
		}
	}
}
