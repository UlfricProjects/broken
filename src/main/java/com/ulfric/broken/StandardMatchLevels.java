package com.ulfric.broken;

public enum StandardMatchLevels implements MatchLevel {

	PERFECT_MATCH(200),
	BLANKET_MATCH(100),
	NOT_MATCHING(0);

	private final int level;

	StandardMatchLevels(int level) {
		this.level = level;
	}

	@Override
	public int toInt() {
		return level;
	}

}
