package com.undead_pixels.dungeon_bots.utils.generic;

import java.io.Serializable;

public class Pair<T,U> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected T first;
	protected U second;

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public Pair<T,U> setFirst(T first) {
		this.first = first;
		return this;
	}

	public Pair<T,U> setSecond(U second) {
		this.second = second;
		return this;
	}

	public T getFirst() {
		return this.first;
	}

	public U getSecond() {
		return this.second;
	}
}
