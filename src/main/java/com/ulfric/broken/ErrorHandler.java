package com.ulfric.broken;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ErrorHandler {

	private final List<Handler> handlers = new CopyOnWriteArrayList<>();

	public void handle(Throwable thrown) {
		Objects.requireNonNull(thrown, "thrown");

		Problem problem = new Problem(thrown);
		handlers.stream()
				.map(handler -> new SimpleImmutableEntry<>(handler, handler.criteria.apply(thrown)))
				.filter(entry -> entry.getValue().toInt() > 0)
				.sorted((o1, o2) -> Integer.compare(o2.getValue().toInt(), o1.getValue().toInt()))
				.map(Entry::getKey)
				.map(handler -> handler.action)
				.forEach(action -> action.accept(problem));
	}

	public <T> Function<Throwable, T> asFutureHandler() {
		return thrown -> {
			Throwable cause = unwrapGenericException(thrown);
			handle(cause);
			return null;
		};
	}

	private Throwable unwrapGenericException(Throwable thrown) {
		if (thrown.getCause() == null) {
			return thrown;
		}

		Class<?> type = thrown.getClass();
		if (type == ExecutionException.class || type == CompletionException.class) {
			return unwrapGenericException(thrown.getCause());
		}

		return thrown;
	}

	public <X extends Throwable> HandlerBuilder<X> withHandler(Class<X> type) {
		Objects.requireNonNull(type, "type");

		return new HandlerBuilder<>(type);
	}

	public final class HandlerBuilder<X extends Throwable> {
		private final Class<X> type;
		private Criteria criteria;
		private Consumer<X> action;
		private boolean skipIfHandled;

		HandlerBuilder(Class<X> type) {
			this.type = type;
		}

		public void add() {
			Objects.requireNonNull(criteria, "missing criteria");
			Objects.requireNonNull(action, "missing action");

			Handler handler = createHandler(type, criteria, action, skipIfHandled);
			handlers.add(handler);
		}

		public HandlerBuilder<X> setCriteria(Criteria criteria) {
			this.criteria = criteria;
			return this;
		}

		public HandlerBuilder<X> setAction(Consumer<X> action) {
			this.action = action;
			return this;
		}

		public HandlerBuilder<X> skipIfHandled() {
			return setSkipIfHandled(true);
		}

		public HandlerBuilder<X> setSkipIfHandled(boolean skipIfHandled) {
			this.skipIfHandled = skipIfHandled;
			return this;
		}
	}

	private Handler createHandler(Class<? extends Throwable> type, Criteria criteria, Consumer<? extends Throwable> action,
			boolean skipIfHandled) {

		Function<Throwable, MatchLevel> handlerCriteria = createHandlerCriteria(type, criteria);
		Consumer<Problem> handlerAction = createHandlerAction(action, skipIfHandled);
		return new Handler(handlerCriteria, handlerAction);
	}

	private Function<Throwable, MatchLevel> createHandlerCriteria(Class<? extends Throwable> type, Criteria criteria) {
		return thrown -> criteria.apply(type, thrown);
	}

	@SuppressWarnings("unchecked")
	private Consumer<Problem> createHandlerAction(Consumer<? extends Throwable> action, boolean skipIfHandled) {
		return problem -> {
			if (skipIfHandled && problem.handled) {
				return;
			}

			problem.handled = true;
			((Consumer<Throwable>) action).accept(problem.cause);
		};
	}

	private static final class Handler {
		final Function<Throwable, MatchLevel> criteria;
		final Consumer<Problem> action;

		Handler(Function<Throwable, MatchLevel> criteria, Consumer<Problem> action) {
			this.criteria = criteria;
			this.action = action;
		}
	}

	private static final class Problem {
		final Throwable cause;
		boolean handled;

		Problem(Throwable cause) {
			this.cause = cause;
		}
	}

}
