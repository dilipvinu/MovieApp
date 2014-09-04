package com.kopra.movieapp.util;

public enum Rated {

	G("G"),
	PG("PG"),
	PG13("PG-13"),
	R("R"),
	NC17("NC-17"),
	X("X"),
	UR("UR"),
	TVY("TV-Y"),
	TVY7("TV-Y7"),
	TVG("TV-G"),
	TVPG("PG"),
	TV14("TV-14"),
	TVMA("TV-MA"),
	NR("NR");
	
	private final String text;
	
	private Rated(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
