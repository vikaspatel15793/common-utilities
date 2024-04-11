package com.poc.utilities.commons.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DefaultExceptionHandler implements ExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    private static final String DEFAULT_EXCEPTION_MESSAGE_VALUE = "_exmsg";
    private final Map<String, RestError> exceptionMappings;

    public DefaultExceptionHandler(Map<String, RestError> mappings) {
        this.exceptionMappings = mappings;
    }

    @Override
    public RestError getRestError(Throwable t) {

        RestError template = getRestErrorTemplate(t);
        if (template == null) {
            return null;
        }

        RestError.Builder builder = new RestError.Builder();
        builder.setStatus(template.getStatus());
        builder.setCode(template.getCode());
        builder.setMoreInfoUrl(template.getMoreInfoUrl());
        builder.setThrowable(t);

        String msg = getMessage(template.getMessage(), t);
        if (msg != null) {
            builder.setMessage(msg);
        }
        msg = getMessage(template.getDeveloperMessage(), t);
        if (msg != null) {
            builder.setDeveloperMessage(msg);
        }

        return builder.build();
    }

    /**
     * Returns the response status message to return to the client, or {@code null} if no
     * status message should be returned.
     *
     * @return the response status message to return to the client, or {@code null} if no
     * status message should be returned.
     */
    protected String getMessage(String msg, Throwable t) {

        if (msg != null) {
            if (msg.equalsIgnoreCase("null") || msg.equalsIgnoreCase("off")) {
                return null;
            }
            if (msg.equalsIgnoreCase(DEFAULT_EXCEPTION_MESSAGE_VALUE)) {
                msg = t.getMessage();
            }
        }

        return msg;
    }

    private RestError getRestErrorTemplate(Throwable t) {
        Map<String, RestError> mappings = this.exceptionMappings;
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }
        RestError template = null;
        String dominantMapping = null;
        int deepest = Integer.MAX_VALUE;
        for (Map.Entry<String, RestError> entry : mappings.entrySet()) {
            String key = entry.getKey();
            int depth = getDepth(key, t);
            if (depth >= 0 && depth < deepest) {
                deepest = depth;
                dominantMapping = key;
                template = entry.getValue();
            }
        }
        if (template != null && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resolving to RestError template '" + template + "' for exception of type [" + t.getClass().getName() +
                    "], based on exception mapping [" + dominantMapping + "]");
        }
        return template;
    }

    /**
     * Return the depth to the superclass matching.
     * <p>0 means ex matches exactly. Returns -1 if there's no match.
     * Otherwise, returns depth. Lowest depth wins.
     */
    protected int getDepth(String exceptionMapping, Throwable t) {
        return getDepth(exceptionMapping, t.getClass(), 0);
    }

    private int getDepth(String exceptionMapping, Class exceptionClass, int depth) {
        if (exceptionClass.getName().contains(exceptionMapping)) {
            // Found it!
            return depth;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass.equals(Throwable.class)) {
            return -1;
        }
        return getDepth(exceptionMapping, exceptionClass.getSuperclass(), depth + 1);
    }
}
