package com.kopra.movieapp;

import android.os.Bundle;

import com.kopra.movieapp.util.Category;

public class HomeActivity extends BaseActivity {
	
	private static final String TAG = "ListFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager()
				.beginTransaction()
				.add(R.id.container, new CollectionFragment(), TAG)
				.commit();
		}
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setCategory(Category.HOME);
	}
}
