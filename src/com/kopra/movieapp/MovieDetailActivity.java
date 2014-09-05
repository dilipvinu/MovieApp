package com.kopra.movieapp;

import android.os.Bundle;
import android.view.MenuItem;

public class MovieDetailActivity extends BaseActivity {

	private static final String TAG = "MovieDetailFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			String movie = getIntent().getStringExtra("movie");
			getFragmentManager()
				.beginTransaction()
				.add(android.R.id.content, MovieDetailFragment.newInstance(movie), TAG)
				.commit();
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
}
