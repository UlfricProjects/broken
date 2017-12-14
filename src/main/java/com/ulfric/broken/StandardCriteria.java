package com.ulfric.broken;

public enum StandardCriteria implements Criteria {

	INSTANCE_OF {
		@Override
		public MatchLevel apply(Class<? extends Throwable> type, Throwable thrown) {
			return type.isInstance(thrown) ? StandardMatchLevels.BLANKET_MATCH : StandardMatchLevels.NOT_MATCHING;
		}
	},

	EXACT_TYPE_MATCH {
		@Override
		public MatchLevel apply(Class<? extends Throwable> type, Throwable thrown) {
			return type == thrown.getClass() ? StandardMatchLevels.PERFECT_MATCH : StandardMatchLevels.NOT_MATCHING;
		}
	},

	MATCH_EVERYTHING {
		@Override
		public MatchLevel apply(Class<? extends Throwable> type, Throwable thrown) {
			return StandardMatchLevels.PERFECT_MATCH;
		}
	};

}
