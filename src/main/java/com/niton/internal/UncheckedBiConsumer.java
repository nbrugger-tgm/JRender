package com.niton.internal;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface UncheckedBiConsumer<T, U,E extends Exception> {
	static <T,U, E extends Exception> BiConsumer<T,U> unchecked(UncheckedBiConsumer<T, U, E> consumer) {
		return (t,u) -> {
			try {
				consumer.accept(t, u);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	void accept(T t, U u) throws E;
}
