package com.poc.utilities.commons.error;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ConstraintViolationExceptionHandler implements ExceptionHandler {
    @Override
    public RestError getRestError(Throwable throwable) {
        if (throwable instanceof ConstraintViolationException) {
            ConstraintViolationException t = (ConstraintViolationException) throwable;
            Set<ConstraintViolation<?>> violations = t.getConstraintViolations();
            String message = "Validation failed for some properties";
            String developerMessage = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            Collection<Map> fields = getViolatedFields(violations);

            return new RestError.Builder()
                    .setCode(400)
                    .setStatus(400)
                    .setThrowable(throwable)
                    .setMessage(message)
                    .setDeveloperMessage(developerMessage)
                    .addCustomData(FIELDS_PROP_NAME, fields)
                    .build();
        }

        return null;
    }

    public static final String FIELDS_PROP_NAME = "fields";

    public Collection<Map> getViolatedFields(Set<ConstraintViolation<?>> violations) {
        Collection<Map> fields = new ArrayList<>();
        for (ConstraintViolation<?> violation : violations) {
            String name = getViolatedFieldName(violation.getPropertyPath().iterator());
            String message = violation.getMessage();

            if (name != null) {
                fields.add(Field.of(name, message).toMap());
            }
        }

        return fields;
    }

    public String getViolatedFieldName(Iterator<Path.Node> nodeSpliterator) {
        Path.Node latest = null;
        while (nodeSpliterator.hasNext()) {
            latest = nodeSpliterator.next();
        }

        return latest == null ? null : latest.getName();
    }

    public static class Field {
        public static Field of(String name, String message) {
            return new Field(name, message);
        }

        private Field(String name, String message) {
            this.name = name;
            this.message = message;
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new LinkedHashMap<>(2);
            map.put("name", name);
            map.put("message", message);

            return map;
        }

        private final String name;
        private final String message;
    }
}
