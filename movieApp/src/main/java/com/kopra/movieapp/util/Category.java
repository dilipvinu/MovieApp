package com.kopra.movieapp.util;

public enum Category {

	HOME(0),
	BOX_OFFICE(1),
	IN_THEATERS(2),
	OPENING(3),
	UPCOMING(4),
	NONE(-1);
	
	private int mValue;
	
	private Category(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return mValue;
	}
}
