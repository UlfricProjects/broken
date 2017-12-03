package com.ulfric.broken;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ErrorHandlerTest {

	private ErrorHandler handler;

	@BeforeEach
	void setupErrorHandler() {
		handler = new ErrorHandler();
	}

	@Test
	void testHandleNull() {
		Assertions.assertThrows(NullPointerException.class, () -> handler.handle(null));
	}

	@Test
	void testHandleWhileEmptyDoesNothing() {
		handler.handle(new Exception());
	}

	@Test
	void testHandleWithStrictIgnoresGeneric() {
		Consumer<Exception> ignoreMock = Mockito.mock(Consumer.class);

		handler.addStrictTypeHandler(Exception.class, ignoreMock);

		handler.handle(new RuntimeException());

		Mockito.verifyZeroInteractions(ignoreMock);
	}

	@Test
	void testHandleWithStrictRunsOnExactMatch() {
		Consumer<IllegalArgumentException> callMock = Mockito.mock(Consumer.class);

		handler.addStrictTypeHandler(IllegalArgumentException.class, callMock);

		IllegalArgumentException exception = new IllegalArgumentException();
		handler.handle(exception);

		Mockito.verify(callMock, Mockito.times(1)).accept(exception);
	}

	@Test
	void testHandleWithStrictAcceptance() {
		Consumer<IllegalArgumentException> callMock = Mockito.mock(Consumer.class);
		Consumer<Exception> ignoreMock = Mockito.mock(Consumer.class);

		handler.addStrictTypeHandler(IllegalArgumentException.class, callMock);
		handler.addStrictTypeHandler(Exception.class, ignoreMock);

		IllegalArgumentException exception = new IllegalArgumentException();
		handler.handle(exception);

		Mockito.verifyZeroInteractions(ignoreMock);
		Mockito.verify(callMock, Mockito.times(1)).accept(exception);
	}

	@Test
	void testHandleWithInstanceOfRunsGeneric() {
		Consumer<Exception> mock = Mockito.mock(Consumer.class);

		handler.addInstanceOfHandler(Exception.class, mock);

		Exception exception = new RuntimeException();
		handler.handle(exception);

		Mockito.verify(mock, Mockito.times(1)).accept(exception);
	}

	@Test
	void testHandleWithInstanceOfRunsOnExactMatch() {
		Consumer<IllegalArgumentException> callMock = Mockito.mock(Consumer.class);

		handler.addInstanceOfHandler(IllegalArgumentException.class, callMock);

		IllegalArgumentException exception = new IllegalArgumentException();
		handler.handle(exception);

		Mockito.verify(callMock, Mockito.times(1)).accept(exception);
	}

	@Test
	void testHandleWithInstanceOfAcceptance() {
		Consumer<IllegalArgumentException> exactMock = Mockito.mock(Consumer.class);
		Consumer<Exception> genericMock = Mockito.mock(Consumer.class);

		handler.addInstanceOfHandler(IllegalArgumentException.class, exactMock);
		handler.addInstanceOfHandler(Exception.class, genericMock);

		IllegalArgumentException exception = new IllegalArgumentException();
		handler.handle(exception);

		Mockito.verify(exactMock, Mockito.times(1)).accept(exception);
		Mockito.verify(genericMock, Mockito.times(1)).accept(exception);
	}

	@Test
	void testFutureHandlerInCompletableFuture() throws InterruptedException, ExecutionException {
		Consumer<IllegalArgumentException> callMock = Mockito.mock(Consumer.class);

		handler.addStrictTypeHandler(IllegalArgumentException.class, callMock);

		IllegalArgumentException exception = new IllegalArgumentException();
		CompletableFuture.runAsync(() -> {
			throw exception;
		}).exceptionally(handler.asFutureHandler()).get();

		Mockito.verify(callMock, Mockito.times(1)).accept(exception);
	}

}
