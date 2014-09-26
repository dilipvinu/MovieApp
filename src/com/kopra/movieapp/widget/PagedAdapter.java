package com.kopra.movieapp.widget;

import android.content.Context;
import android.widget.ListAdapter;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.kopra.movieapp.R;

public class PagedAdapter extends EndlessAdapter {

	public interface OnCacheListener {
		public void onCache(int page);
	};
	
	private OnCacheListener mCacheListener;
	private int mPage = 1;
	
	public PagedAdapter(Context context, ListAdapter wrapped, boolean keepOnAppending) {
		super(context, wrapped, R.layout.list_item_pending, keepOnAppending);
		setRunInBackground(false);
	}
	
	public void setOnCacheListener(OnCacheListener listener) {
		mCacheListener = listener;
	}
	
	public void setPage(int page) {
		mPage = page;
	}
	
	public void onDataReady(boolean keepOnAppending) {
		super.onDataReady();
		if (!keepOnAppending) {
			stopAppending();
		}
	}

	@Override
	protected void appendCachedData() {
	}

	@Override
	protected boolean cacheInBackground() throws Exception {
		if (mCacheListener != null) {
			mCacheListener.onCache(++mPage);
		}
		return true;
	}

}
