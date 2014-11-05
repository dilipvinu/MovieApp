package com.kopra.movieapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.kopra.movieapp.util.Category;
import com.kopra.movieapp.util.Consts;

public class HomeActivity extends BaseActivity {
	
	private static final String TAG = "ListFragment";
	
	private int mCurrentPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			setCategory(Category.HOME);
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.container, new CollectionFragment(), TAG)
				.commit();
		}
		getFragmentManager().addOnBackStackChangedListener(onBackStackChanged);
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
			mCurrentPosition = position;
			if (clearStack) {
				getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			}
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, fragment, TAG);
			if (!clearStack) {
				transaction.addToBackStack(null);
			}
			transaction.commit();
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
				mCurrentPosition = type;
				switch (type) {
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
					setCategory(Category.HOME);
					break;
				}
			}
		}
	};
}
