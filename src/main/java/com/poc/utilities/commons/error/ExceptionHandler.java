package com.poc.utilities.commons.error;

public interface ExceptionHandler<T extends Throwable> {
    RestError getRestError(T throwable);
}
