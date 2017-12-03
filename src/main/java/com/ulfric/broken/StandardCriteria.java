package com.ulfric.broken;

import java.util.function.BiPredicate;

public enum StandardCriteria implements BiPredicate<Class<? extends Throwable>, Throwable> {

	INSTANCE_OF {
		@Override
		public boolean test(Class<? extends Throwable> type, Throwable thrown) {
			return type.isInstance(thrown);
		}
	},

	EXACT_TYPE_MATCH {
		@Override
		public boolean test(Class<? extends Throwable> type, Throwable thrown) {
			return type == thrown.getClass();
		}
	},

	MATCH_EVERYTHING {
		@Override
		public boolean test(Class<? extends Throwable> type, Throwable thrown) {
			return true;
		}
	};

}
