package com.ulfric.broken;

import java.util.function.BiFunction;

public interface Criteria extends BiFunction<Class<? extends Throwable>, Throwable, MatchLevel> {

}
