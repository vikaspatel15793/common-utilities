package com.poc.utilities.commons.error;

import com.poc.utilities.commons.lang.ClassUtils;
import com.poc.utilities.commons.lang.OrderPreservingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.InputStream;
import java.util.Map;

public class DefaultRestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger loggerj = LoggerFactory.getLogger(DefaultRestExceptionHandler.class);
    private static final HttpHeaders headers = new HttpHeaders();
    private static DefaultExceptionHandler defaultExceptionHandler;

    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
        loggerj.debug("Initializing rest error templates");
        ExceptionTemplateProvider defaultProvider = TemplateProviders.getDefaultProvider();
        defaultExceptionHandler = new DefaultExceptionHandler(defaultProvider.getExceptionMappings());
    }

    @ResponseBody
    @org.springframework.web.bind.annotation.ExceptionHandler({Throwable.class})
    public ResponseEntity<Map> handleBadRequestCustomException(Throwable t) {
        return getMapResponseEntity(t);
    }

    private ResponseEntity<Map> getMapResponseEntity(Throwable t) {
        return (ResponseEntity<Map>) getResponseHandlerError(t);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException t, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return (ResponseEntity<Object>) getResponseHandlerError(t);
    }

    private ResponseEntity<?> getResponseHandlerError(Throwable t) {
        ExceptionHandler handler = findHandler(t, defaultExceptionHandler);
        RestError error = handler.getRestError(t);

        return new ResponseEntity<>(error.toMap(), HttpStatus.valueOf(error.getStatus()));
    }

    public static ExceptionHandler findHandler(Throwable throwable, ExceptionHandler fallbackHandler) {
        ExceptionHandler handler = findHandler(throwable);
        if (handler == null) {
            return fallbackHandler;
        }

        return handler;
    }

    public static ExceptionHandler findHandler(Throwable throwable) {
        InputStream is = ClassUtils.getResourceAsStream("errorHandlers.properties");

        if (is == null) {
            return null;
        }

        OrderPreservingProperties props = new OrderPreservingProperties();
        props.load(is);

        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (throwable.getClass().isAssignableFrom(ClassUtils.forName(key))) {
                try {
                    Class<? extends ExceptionHandler> handlerClass = ClassUtils.forName(value);
                    return handlerClass.cast(ClassUtils.newInstance(handlerClass));
                } catch (RuntimeException e) {
                    loggerj.error(e.getMessage(), e);
                }
            }
        }

        return null;
    }
}