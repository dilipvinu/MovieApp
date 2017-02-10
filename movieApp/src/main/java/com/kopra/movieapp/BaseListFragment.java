package com.kopra.movieapp;

import com.etsy.android.grid.StaggeredGridView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;

public class BaseListFragment extends Fragment {

	private StaggeredGridView mList;
	private View mEmpty;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mList = (StaggeredGridView) view.findViewById(android.R.id.list);
		mEmpty = view.findViewById(android.R.id.empty);
		mList.setEmptyView(mEmpty);
		mList.setOnItemClickListener(onItemClick);
		mList.setOnScrollListener(onScroll);
	}
	
	public void onListItemClick(AbsListView parent, View view, int position, long id) {
	}
	
	public void onListScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}
	
	public ListAdapter getListAdapter() {
		return mList.getAdapter();
	}
	
	public void setListAdapter(ListAdapter adapter) {
		mList.setAdapter(adapter);
	}
	
	protected void addHeaderView(View view) {
		mList.addHeaderView(view);
	}
	
	protected void removeHeaderView(View view) {
		mList.removeHeaderView(view);
	}
	
	protected void addFooterView(View view) {
		mList.addFooterView(view);
	}
	
	protected void removeFooterView(View view) {
		mList.removeFooterView(view);
	}
	
	private OnItemClickListener onItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			onListItemClick(mList, view, position, id);
		}
	};
	
	private OnScrollListener onScroll = new OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			onListScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int state) {
		}
	};
}
