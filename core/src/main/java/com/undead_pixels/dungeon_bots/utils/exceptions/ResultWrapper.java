package com.undead_pixels.dungeon_bots.utils.exceptions;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Class that is a functional wrapper around a result type from a Supplier<br>
 *     that can throw an exception for convenience.
 * @param <T> The result type.
 */
public final class ResultWrapper<T> {
	private Optional<T> val = Optional.empty();
	private Optional<Throwable> throwable = Optional.empty();

	public interface ThrowableSupplier<U> {
		U get() throws Throwable;
	}

	public ResultWrapper(ThrowableSupplier<T> supplier) {
		try { val = Optional.ofNullable(supplier.get()); }
		catch (Throwable t) { throwable = Optional.of(t); }
	}

	public ResultWrapper ifResult(Consumer<T> fn) {
		val.ifPresent(fn);
		return this;
	}

	public Optional<T> result() {
		return val;
	}

	public T getResult() {
		return val.get();
	}

	public ResultWrapper ifError(Consumer<Throwable> fn) {
		throwable.ifPresent(fn);
		return this;
	}

	public Optional<Throwable> error() {
		return throwable;
	}

	public Throwable getError() {
		return throwable.get();
	}

	public boolean hasResult() {
		return val.isPresent();
	}

	public boolean hasError() {
		return throwable.isPresent();
	}

	public static <U> ResultWrapper<U> result(ThrowableSupplier<U> supplier) {
		return new ResultWrapper<>(supplier);
	}
}
