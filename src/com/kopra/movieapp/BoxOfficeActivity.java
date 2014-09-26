package com.kopra.movieapp;

import android.content.Intent;
import android.os.Bundle;

import com.kopra.movieapp.util.Category;
import com.kopra.movieapp.util.Consts;

public class BoxOfficeActivity extends BaseActivity {

	private static final String TAG = "ListFragment";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			setCategory(Category.BOX_OFFICE);
			getFragmentManager()
				.beginTransaction()
				.add(R.id.container, MovieListFragment.newInstance(Consts.List.BOX_OFFICE, null, false), TAG)
				.commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		overridePendingTransition(0, 0);
	}
}
