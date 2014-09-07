package com.kopra.movieapp;

import com.kopra.movieapp.util.Category;
import com.kopra.movieapp.util.Consts;

import android.os.Bundle;

public class BoxOfficeActivity extends BaseActivity {

	private static final String TAG = "ListFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager()
				.beginTransaction()
				.add(R.id.container, MovieListFragment.newInstance(Consts.List.BOX_OFFICE, null), TAG)
				.commit();
		}
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setCategory(Category.BOX_OFFICE);
	}
	
}
