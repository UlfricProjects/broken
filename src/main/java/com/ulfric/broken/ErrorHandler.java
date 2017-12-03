package com.ulfric.broken;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ErrorHandler {

	private final List<Handler> handlers = new CopyOnWriteArrayList<>();

	public void handle(Throwable thrown) {
		Objects.requireNonNull(thrown, "thrown");

		Problem problem = new Problem(thrown);
		handlers.stream().filter(handler -> handler.criteria.test(problem))
				.map(handler -> handler.action)
				.forEach(action -> action.accept(problem));
	}

	public <T> Function<Throwable, T> asFutureHandler() {
		return thrown -> {
			handle(thrown.getCause());
			return null;
		};
	}

	public <X extends Throwable> HandlerBuilder<X> withHandler(Class<X> type) {
		Objects.requireNonNull(type, "type");

		return new HandlerBuilder<>(type);
	}

	public final class HandlerBuilder<X extends Throwable> {
		private final Class<X> type;
		private BiPredicate<Class<? extends Throwable>, Throwable> criteria;
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

		public HandlerBuilder<X> setCriteria(BiPredicate<Class<? extends Throwable>, Throwable> criteria) {
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

	private Handler createHandler(Class<? extends Throwable> type,
			BiPredicate<Class<? extends Throwable>, Throwable> criteria, Consumer<? extends Throwable> action,
			boolean skipIfHandled) {

		Predicate<Problem> problemCriteria = createProblemCriteria(type, criteria, skipIfHandled);
		Consumer<Problem> problemAction = createProblemAction(action);
		return new Handler(problemCriteria, problemAction);
	}

	private Predicate<Problem> createProblemCriteria(Class<? extends Throwable> type,
			BiPredicate<Class<? extends Throwable>, Throwable> criteria, boolean skipIfHandled) {

		return problem -> {
			if (skipIfHandled) {
				if (problem.handled) {
					return false;
				}
			}

			return criteria.test(type, problem.cause);
		};
	}

	@SuppressWarnings("unchecked")
	private Consumer<Problem> createProblemAction(Consumer<? extends Throwable> action) {
		Consumer<Problem> problemAction = problem -> {
			problem.handled = true;
		};

		return problemAction.andThen(problem -> ((Consumer<Throwable>) action).accept(problem.cause));
	}

	private static final class Handler {
		final Predicate<Problem> criteria;
		final Consumer<Problem> action;

		Handler(Predicate<Problem> criteria, Consumer<Problem> action) {
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
