package com.ulfric.broken;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ErrorHandler {

	private final List<Handler> handlers = new CopyOnWriteArrayList<>();

	@SuppressWarnings("unchecked")
	public void handle(Throwable thrown) {
		Objects.requireNonNull(thrown, "thrown");

		handlers.stream()
			.filter(handler -> handler.criteria.test(thrown))
			.map(handler -> (Consumer<Throwable>) handler.action)
			.forEach(action -> action.accept(thrown));
	}

	public <T> Function<Throwable, T> asFutureHandler() {
		return thrown -> {
			handle(thrown.getCause());
			return null;
		};
	}

	public <X extends Throwable> void addStrictTypeHandler(Class<X> type, Consumer<X> action) {
		Objects.requireNonNull(type, "type");

		addHandler(exception -> exception.getClass() == type, action);
	}

	public <X extends Throwable> void addInstanceOfHandler(Class<X> type, Consumer<X> action) {
		Objects.requireNonNull(type, "type");

		addHandler(type::isInstance, action);
	}

	private void addHandler(Predicate<Throwable> criteria, Consumer<? extends Throwable> action) {
		Objects.requireNonNull(criteria, "criteria");
		Objects.requireNonNull(action, "action");

		handlers.add(new Handler(criteria, action));
	}

	static final class Handler {
		final Predicate<Throwable> criteria;
		final Consumer<? extends Throwable> action;

		Handler(Predicate<Throwable> criteria, Consumer<? extends Throwable> action) {
			this.criteria = criteria;
			this.action = action;
		}
	}

}
