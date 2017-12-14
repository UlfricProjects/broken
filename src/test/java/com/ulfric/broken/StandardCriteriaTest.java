package com.ulfric.broken;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

class StandardCriteriaTest {

	@Test
	void testMatchEverythingIsTrue() {
		Truth.assertThat(StandardCriteria.MATCH_EVERYTHING.apply(null, null)).isSameAs(StandardMatchLevels.PERFECT_MATCH);
	}

}
