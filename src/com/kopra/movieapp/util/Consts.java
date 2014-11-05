package com.kopra.movieapp.util;

public class Consts {

	public static final class Config {
		public static final int		TIMEOUT = 5000;
		public static final int		LIMIT_SUGGESTION = 5;
		public static final int		LIMIT_SEARCH = 18;
		public static final int		LIMIT_COLLECTION = 6;
		public static final int		MAX_PAGE = 25;
	}
	
	public static final class Api {
		public static final String	BASE = "http://api.rottentomatoes.com/api/public/v1.0";
		public static final String	SEARCH = "/movies.json?q=%s&page_limit={limit}&page={page}&apikey={key}";
		public static final String	BOX_OFFICE = "/lists/movies/box_office.json?limit={limit}&country=us&apikey={key}";
		public static final String	IN_THEATERS = "/lists/movies/in_theaters.json?page_limit={limit}&page={page}&country=us&apikey={key}";
		public static final String	OPENING = "/lists/movies/opening.json?limit={limit}&country=us&apikey={key}";
		public static final String	UPCOMING = "/lists/movies/upcoming.json?page_limit={limit}&page={page}&country=us&apikey={key}";
		public static final String	MOVIE_DETAIL = "/movies/%s.json?apikey={key}";
		public static final String	SIMILAR = "/movies/%s/similar.json?limit={limit}&apikey={key}";
		
		public static final String	BASE_OMDB = "http://www.omdbapi.com";
		public static final String	MOVIE_DETAIL_OMDB = "/?i=tt%s";
	}
	
	public static final class List {
		public static final int		SEARCH = -1;
		public static final int		BOX_OFFICE = 1;
		public static final int		IN_THEATERS = 2;
		public static final int		OPENING = 3;
		public static final int		UPCOMING = 4;
	}
	
}
