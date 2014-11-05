package com.kopra.movieapp;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import com.kopra.movieapp.util.Category;

public class BaseActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	protected MenuItem mSearchMenu;
	
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;
	private int mCurrentSelectedPosition = 0;
	private Category mCurrentCategory = Category.NONE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, R.layout.activity_drawer);
	}
	
	protected void onCreate(Bundle savedInstanceState, int layoutResID) {
		super.onCreate(savedInstanceState);
		setContentView(layoutResID);
		
		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt("selected_position");
		}
		
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (mSearchMenu != null) {
			mSearchMenu.collapseActionView();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("selected_position", mCurrentSelectedPosition);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mTitle = title;
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
//		if (position == mCurrentSelectedPosition) {
//			return;
//		}
//		
//		try {
//			Class<?> cls = null;
//			Category category = Category.values()[position];
//			switch (category) {
//			case HOME:
//				cls = HomeActivity.class;
//				break;
//			case BOX_OFFICE:
//				cls = BoxOfficeActivity.class;
//				break;
//			case IN_THEATERS:
//				cls = InTheatersActivity.class;
//				break;
//			case OPENING:
//				cls = OpeningMoviesActivity.class;
//				break;
//			case UPCOMING:
//				cls = UpcomingMoviesActivity.class;
//				break;
//			default:
//				return;
//			}
//			Intent intent = new Intent(this, cls);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//			startActivity(intent);
//			overridePendingTransition(0, 0);
//		} catch (IndexOutOfBoundsException e) {}
	}
	
	public Category getCategory() {
		return mCurrentCategory;
	}
	
	public void setCategory(Category category) {
		mCurrentCategory = category;
		mCurrentSelectedPosition = category.getValue();
		mNavigationDrawerFragment.selectItem(category.getValue());
	}
	
	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}
	
	public void setDrawerIndicatorEnabled(boolean enable) {
		mNavigationDrawerFragment.setDrawerIndicatorEnabled(enable);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mNavigationDrawerFragment.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.base, menu);
			
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			mSearchMenu = menu.findItem(R.id.action_search);
			SearchView searchView = (SearchView) MenuItemCompat.getActionView(mSearchMenu);
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}
	
}
