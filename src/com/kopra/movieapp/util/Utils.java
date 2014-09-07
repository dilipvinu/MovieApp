package com.kopra.movieapp.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {

	public static String getRottenApiKey(Context context) {
		ApplicationInfo info;
		try {
			info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return info.metaData.getString("com.rotten.apikey");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getUrlWithKey(Context context, String url) {
		return url.replace("{key}", getRottenApiKey(context));
	}
	
	public static String encode(String url) {
		if (url == null) {
			return null;
		}
		
		try {
			return URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return url;
		}
	}
	
	public static JSONObject toJson(String json) {
		if (json == null) {
			return null;
		}

		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			return null;
		}
	}
	
	public static String getApiMethod(int type) {
		switch (type) {
		case Consts.List.SEARCH:
			return Consts.Api.SEARCH;
		case Consts.List.BOX_OFFICE:
			return Consts.Api.BOX_OFFICE;
		case Consts.List.IN_THEATERS:
			return Consts.Api.IN_THEATERS;
		case Consts.List.OPENING:
			return Consts.Api.OPENING;
		case Consts.List.UPCOMING:
			return Consts.Api.UPCOMING;
		default:
			return null;
		}
	}
}
