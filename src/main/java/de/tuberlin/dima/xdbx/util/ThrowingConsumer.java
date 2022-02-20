package de.tuberlin.dima.xdbx.util;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;
    default ThrowingConsumer<T, E> andThen(ThrowingConsumer<T, E> consumer){
        return (t) -> {
            accept(t);
            consumer.accept(t);
        };
    }
}
