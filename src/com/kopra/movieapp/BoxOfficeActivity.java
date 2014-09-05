package com.kopra.movieapp;

import com.kopra.movieapp.util.Consts;

import android.os.Bundle;

public class BoxOfficeActivity extends BaseActivity {

private static final String TAG = "BoxOfficeFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager()
				.beginTransaction()
				.add(android.R.id.content, MovieListFragment.newInstance(Consts.List.BOX_OFFICE, null), TAG)
				.commit();
		}
	}
	
}
