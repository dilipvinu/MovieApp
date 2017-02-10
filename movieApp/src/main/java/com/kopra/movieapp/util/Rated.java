package com.kopra.movieapp.util;

import com.kopra.movieapp.R;

public class Rated {

	public static int getDrawable(String rated) {
		if ("G".equals(rated)) {
			return R.drawable.cert_us_g;
		}
		if ("PG".equals(rated)) {
			return R.drawable.cert_us_pg;
		}
		if ("PG-13".equals(rated)) {
			return R.drawable.cert_us_pg13;
		}
		if ("R".equals(rated)) {
			return R.drawable.cert_us_r;
		}
		if ("NC-17".equals(rated)) {
			return R.drawable.cert_us_nc17;
		}
		if ("X".equals(rated)) {
			return R.drawable.cert_us_x;
		}
		if ("UNRATED".equals(rated)) {
			return R.drawable.cert_us_unrated;
		}
		if ("NOT RATED".equals(rated)) {
			return 0;
		}
		if ("TV-Y".equals(rated)) {
			return R.drawable.cert_us_tvy;
		}
		if ("TV-Y7".equals(rated)) {
			return R.drawable.cert_us_tvy7;
		}
		if ("TV-G".equals(rated)) {
			return R.drawable.cert_us_tvg;
		}
		if ("TV-PG".equals(rated)) {
			return R.drawable.cert_us_tvpg;
		}
		if ("TV-14".equals(rated)) {
			return R.drawable.cert_us_tv14;
		}
		if ("TV-MA".equals(rated)) {
			return R.drawable.cert_us_tvma;
		}
		return 0;
	}
}
