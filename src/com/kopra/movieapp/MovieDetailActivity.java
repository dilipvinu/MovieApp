package com.kopra.movieapp;

import com.kopra.movieapp.util.Category;

import android.os.Bundle;
import android.view.MenuItem;

public class MovieDetailActivity extends BaseActivity {

	private static final String TAG = "MovieDetailFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setDrawerIndicatorEnabled(false);
		if (savedInstanceState == null) {
			setCategory(Category.NONE);
			String movie = getIntent().getStringExtra("movie");
			getFragmentManager()
				.beginTransaction()
				.add(R.id.container, MovieDetailFragment.newInstance(movie), TAG)
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
