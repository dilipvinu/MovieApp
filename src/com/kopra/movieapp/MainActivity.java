package com.kopra.movieapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import com.kopra.movieapp.util.Category;
import com.kopra.movieapp.util.Consts;

public class MainActivity extends BaseActivity {
	
	private static final String TAG = "ListFragment";
	
	private int mCurrentPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.container, new CollectionFragment(), TAG)
				.commit();
		} else {
			String title = ((TypeFragment) getFragmentManager().findFragmentById(R.id.container)).getTitle();
			setTitle(title);
			getSupportActionBar().setTitle(title);
		}
		getFragmentManager().addOnBackStackChangedListener(onBackStackChanged);
	}
	
	@Override
	protected void onNewIntent(Intent newIntent) {
		setIntent(newIntent);
		handleIntent(getIntent());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
			return;
		}
		super.onBackPressed();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!super.onOptionsItemSelected(item)) {
			switch (item.getItemId()) {
			case android.R.id.home:
				getFragmentManager().popBackStack();
				break;
			}
		}
		return true;
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if (mCurrentPosition == position) {
			return;
		}
		
		try {
			Fragment fragment;
			boolean clearStack = false;
			Category category = Category.values()[position];
			switch (category) {
			case HOME:
				fragment = new CollectionFragment();
				clearStack = true;
				setTitle(getString(R.string.home));
				getSupportActionBar().setTitle(getString(R.string.home));
				break;
			case BOX_OFFICE:
				fragment = MovieListFragment.newInstance(Consts.List.BOX_OFFICE, getString(R.string.box_office), null, false);
				break;
			case IN_THEATERS:
				fragment = MovieListFragment.newInstance(Consts.List.IN_THEATERS, getString(R.string.in_theaters), null, true);
				break;
			case OPENING:
				fragment = MovieListFragment.newInstance(Consts.List.OPENING, getString(R.string.opening), null, false);
				break;
			case UPCOMING:
				fragment = MovieListFragment.newInstance(Consts.List.UPCOMING, getString(R.string.upcoming), null, true);
				break;
			default:
				return;
			}
			addFragment(position, fragment, clearStack);
		} catch (IndexOutOfBoundsException e) {}
	}
	
	private OnBackStackChangedListener onBackStackChanged = new OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
			if (fragment instanceof TypeFragment) {
				int type = ((TypeFragment) fragment).getType();
				String title = ((TypeFragment) fragment).getTitle();
				setTitle(title);
				getSupportActionBar().setTitle(title);
				setDrawerIndicatorEnabled(type >= 0);
				mCurrentPosition = type;
				switch (type) {
				case 0:
					setCategory(Category.HOME);
					break;
				case Consts.List.BOX_OFFICE:
					setCategory(Category.BOX_OFFICE);
					break;
				case Consts.List.IN_THEATERS:
					setCategory(Category.IN_THEATERS);
					break;
				case Consts.List.OPENING:
					setCategory(Category.OPENING);
					break;
				case Consts.List.UPCOMING:
					setCategory(Category.UPCOMING);
					break;
				default:
					setCategory(Category.NONE);
					break;
				}
			}
		}
	};
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String movie = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
			Fragment fragment = MovieDetailFragment.newInstance(movie);
			addFragment(-1, fragment, false);
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Fragment fragment = MovieListFragment.newInstance(Consts.List.SEARCH, getString(R.string.app_name), query, true);
			addFragment(-1, fragment, false);
		}
	}
	
	public void addFragment(int position, final Fragment fragment, boolean clearStack) {
		mCurrentPosition = position;
		if (clearStack) {
			getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FragmentTransaction transaction = getFragmentManager().beginTransaction();
					transaction.replace(R.id.container, fragment, TAG);
					transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					transaction.addToBackStack(null);
					transaction.commit();
				}
			}, 300);
		}
	}
}
