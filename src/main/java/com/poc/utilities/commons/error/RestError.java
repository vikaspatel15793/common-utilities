package com.poc.utilities.commons.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class
RestError {
    private static final String STATUS_PROP_NAME = "status";
    private static final String CODE_PROP_NAME = "code";
    private static final String MESSAGE_PROP_NAME = "message";
    private static final String DEVELOPER_MESSAGE_PROP_NAME = "developerMessage";
    private static final String MORE_INFO_PROP_NAME = "moreInfo";

    private static final String DEFAULT_MORE_INFO_URL = "mailto:utilities@commons.com";

    private final int status;
    private final int code;
    private final String message;
    private final String developerMessage;
    private final String moreInfoUrl;
    private final Throwable throwable;
    private Map<String, Object> customData;

    public RestError(int status, int code, String message, String developerMessage, String moreInfoUrl, Throwable throwable) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.developerMessage = developerMessage;
        this.moreInfoUrl = moreInfoUrl;
        this.throwable = throwable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestError restError = (RestError) o;
        return code == restError.code &&
                status == restError.status &&
                Objects.equals(message, restError.message) &&
                Objects.equals(developerMessage, restError.developerMessage) &&
                Objects.equals(moreInfoUrl, restError.moreInfoUrl) &&
                Objects.equals(throwable, restError.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, code, message, developerMessage, moreInfoUrl, throwable);
    }

    public String toString() {
        return append(new StringBuilder(), getStatus())
                .append(", message: ").append(getMessage())
                .append(", code: ").append(getCode())
                .append(", developerMessage: ").append(getDeveloperMessage())
                .append(", moreInfoUrl: ").append(getMoreInfoUrl())
                .toString();
    }

    private StringBuilder append(StringBuilder buf, int status) {
        buf.append(status);
        return buf;
    }

    private String toString(int status) {
        return append(new StringBuilder(), status).toString();
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getMoreInfoUrl() {
        return moreInfoUrl;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }

    public Map<String, ?> toMap() {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        int status = getStatus();
        m.put(STATUS_PROP_NAME, status);

        int code = getCode();
        if (code <= 0) {
            code = status;
        }
        m.put(CODE_PROP_NAME, code);

        String httpStatusMessage = null;

        String message = getMessage();
        if (message == null) {
            httpStatusMessage = toString(status);
            message = httpStatusMessage;
        }
        m.put(MESSAGE_PROP_NAME, message);

        String devMsg = getDeveloperMessage();
        if (devMsg == null) {
            if (httpStatusMessage == null) {
                httpStatusMessage = toString(status);
            }
            devMsg = httpStatusMessage;

            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
            Throwable t = getThrowable();
            if (t != null) {
                devMsg = devMsg + ": " + t.getMessage();
            }
        }
        m.put(DEVELOPER_MESSAGE_PROP_NAME, devMsg);

        String moreInfoUrl = getMoreInfoUrl();
        if (moreInfoUrl == null) {
            moreInfoUrl = DEFAULT_MORE_INFO_URL;
        }
        m.put(MORE_INFO_PROP_NAME, moreInfoUrl);

        if (customData != null && !customData.isEmpty()) {
            m.putAll(customData);
        }

        return m;
    }

    public static class Builder {

        private int status;
        private int code;
        private String message;
        private String developerMessage;
        private String moreInfoUrl;
        private Throwable throwable;
        private Map<String, Object> customData = new LinkedHashMap<>();

        public Builder setStatus(int statusCode) {
            this.status = statusCode;
            return this;
        }

        public Builder setCode(int code) {
            this.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setDeveloperMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }

        public Builder setMoreInfoUrl(String moreInfoUrl) {
            this.moreInfoUrl = moreInfoUrl;
            return this;
        }

        public Builder setThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public Builder addCustomData(String key, Object value) {
            this.customData.put(key, value);
            return this;
        }

        public RestError build() {
            if (this.status == 0) {
                this.status = 500;
            }
            if (this.moreInfoUrl == null) {
                this.moreInfoUrl = DEFAULT_MORE_INFO_URL;
            }
            RestError restError = new RestError(this.status, this.code, this.message, this.developerMessage, this.moreInfoUrl, this.throwable);
            restError.setCustomData(customData);

            return restError;
        }
    }
}
