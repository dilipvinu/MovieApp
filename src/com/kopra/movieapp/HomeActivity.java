package com.kopra.movieapp;

import android.os.Bundle;

import com.kopra.movieapp.util.Category;

public class HomeActivity extends BaseActivity {
	
	private static final String TAG = "ListFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			setCategory(Category.HOME);
			getFragmentManager()
				.beginTransaction()
				.add(R.id.container, new CollectionFragment(), TAG)
				.commit();
		}
	}
}
