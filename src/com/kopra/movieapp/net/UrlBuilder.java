package com.kopra.movieapp.net;

import android.content.Context;

import com.kopra.movieapp.util.Utils;

public class UrlBuilder {

	private Context mContext;
	private String mBase;
	private String mMethod;
	private int mPage;
	private int mLimit;
	
	public UrlBuilder(Context context) {
		this.mContext = context;
	}
	
	public UrlBuilder setBase(String base) {
		this.mBase = base;
		return this;
	}
	
	public UrlBuilder setMethod(String method) {
		this.mMethod = method;
		return this;
	}
	
	public UrlBuilder setPage(int page) {
		this.mPage = page;
		return this;
	}
	
	public UrlBuilder setLimit(int limit) {
		this.mLimit = limit;
		return this;
	}
	
	public String build() {
		String url = mBase + mMethod;
		return Utils.getUrlWithKey(mContext, 
				url.replace("{page}", String.valueOf(mPage)).replace("{limit}", String.valueOf(mLimit)));
	}
}
