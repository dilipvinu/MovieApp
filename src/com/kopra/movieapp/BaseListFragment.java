package com.kopra.movieapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;

public class BaseListFragment extends Fragment {

	private AbsListView mList;
	private View mEmpty;
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mList = (AbsListView) view.findViewById(android.R.id.list);
		mEmpty = view.findViewById(android.R.id.empty);
		mList.setEmptyView(mEmpty);
		mList.setOnItemClickListener(onItemClick);
	}
	
	public void onListItemClick(AbsListView parent, View view, int position, long id) {
	}
	
	public void setListAdapter(ListAdapter adapter) {
		mList.setAdapter(adapter);
	}
	
	private OnItemClickListener onItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			onListItemClick(mList, view, position, id);
		}
	};
}
