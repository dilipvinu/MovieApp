package com.kopra.movieapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class BaseActivity extends Activity {

	protected MenuItem mSearchMenu;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.base, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		mSearchMenu = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) mSearchMenu.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		return true;
	}
}
