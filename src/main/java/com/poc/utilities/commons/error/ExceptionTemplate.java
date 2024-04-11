package com.poc.utilities.commons.error;

public class ExceptionTemplate {
    private Class<? extends Throwable> aClass;
    private int status;
    private String message;
    private boolean delegated;
    private ExceptionHandler handler;

    public ExceptionTemplate(Class<? extends Throwable> aClass, int status, String message) {
        this.aClass = aClass;
        this.status = status;
        this.message = message;
    }

    public ExceptionTemplate(Class<? extends Throwable> aClass, int status, String message, ExceptionHandler handler) {
        this.aClass = aClass;
        this.status = status;
        this.message = message;
        this.handler = handler;
        this.delegated = true;
    }

    public boolean isDelegated() {
        return delegated;
    }

    public void setDelegated(boolean delegated) {
        this.delegated = delegated;
    }

    public ExceptionHandler getHandler() {
        return handler;
    }

    public void setHandler(ExceptionHandler handler) {
        this.handler = handler;
    }

    public Class<? extends Throwable> getaClass() {
        return aClass;
    }

    public void setaClass(Class<? extends Throwable> aClass) {
        this.aClass = aClass;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
