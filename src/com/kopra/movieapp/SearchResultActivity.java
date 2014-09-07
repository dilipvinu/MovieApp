package com.kopra.movieapp;

import android.os.Bundle;

import com.kopra.movieapp.util.Category;
import com.kopra.movieapp.util.Consts;

public class SearchResultActivity extends BaseActivity {

	private static final String TAG = "ListFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			String query = getIntent().getStringExtra("query");
			getFragmentManager()
					.beginTransaction()
					.add(R.id.container, MovieListFragment.newInstance(Consts.List.SEARCH, query), TAG)
					.commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setCategory(Category.NONE);
	}
}