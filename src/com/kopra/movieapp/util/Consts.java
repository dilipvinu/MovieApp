package com.kopra.movieapp.util;

public class Consts {

	public static final class Config {
		public static final int		TIMEOUT = 5000;
	}
	
	public static final class Api {
		public static final String	SUGGESTIONS = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?q=%s&page_limit=5&page=1&apikey={key}";
		public static final String	SEARCH = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?q=%s&page_limit=16&page=1&apikey={key}";
		public static final String	BOX_OFFICE = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?limit=16&country=us&apikey={key}";
		public static final String	MOVIE_DETAIL = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s.json?apikey={key}";
		public static final String	MOVIE_DETAIL_OMDB = "http://www.omdbapi.com/?i=tt%s";
	}
	
	public static final class List {
		public static final int		SEARCH = 0;
		public static final int		BOX_OFFICE = 1;
		public static final int		IN_THEATERS = 2;
	}
}
