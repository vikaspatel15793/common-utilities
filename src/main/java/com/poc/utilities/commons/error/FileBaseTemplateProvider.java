package com.poc.utilities.commons.error;


import com.poc.utilities.commons.lang.ClassUtils;
import com.poc.utilities.commons.lang.OrderPreservingProperties;
import com.poc.utilities.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.*;


public class FileBaseTemplateProvider implements ExceptionTemplateProvider {
    public static final String EXCEPTION_CONFIG_DELIMITER = "|";
    private final Map<String, RestError> exceptionMappings;

    public FileBaseTemplateProvider() {
        InputStream errorStream = ClassUtils.getResourceAsStream("restErrors.properties");
        InputStream defaultErrorStream = ClassUtils.getResourceAsStream("defaultRestErrors.properties");

        OrderPreservingProperties props = new OrderPreservingProperties();
        props.load(defaultErrorStream);
        props.load(errorStream);
        this.exceptionMappings = toRestErrors(props);
    }

    @Override
    public ExceptionTemplate[] getTemplates() {
        return new ExceptionTemplate[ 0 ];
    }

    @Override
    public Map<String, RestError> getExceptionMappings() {
        return exceptionMappings;
    }

    private static Map<String, RestError> toRestErrors(Map<String, String> smap) {
        if (smap == null || smap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, RestError> map = new LinkedHashMap<>(smap.size());

        for (Map.Entry<String, String> entry : smap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            RestError template = toRestError(value);
            map.put(key, template);
        }

        return map;
    }

    private static RestError toRestError(String exceptionConfig) {
        String[] values = StringUtils.delimitedListToStringArray(exceptionConfig, EXCEPTION_CONFIG_DELIMITER);
        if ( values.length == 0) {
            throw new IllegalStateException("Invalid config mapping.  Exception names must map to a string configuration.");
        }

        if (values.length > 5) {
            throw new IllegalStateException("Invalid config mapping.  Mapped values must not contain more than 2 " +
                    "values (code=y, msg=z, devMsg=x)");
        }

        RestError.Builder builder = new RestError.Builder();

        boolean statusSet = false;
        boolean codeSet = false;
        boolean msgSet = false;
        boolean devMsgSet = false;
        boolean moreInfoSet = false;

        for (String value : values) {

            String trimmedVal = StringUtils.trimWhitespace(value);

            //check to see if the value is an explicitly named key/value pair:
            String[] pair = StringUtils.split(trimmedVal, "=");
            if (pair.length > 1) {
                //explicit attribute set:
                String pairKey = StringUtils.trimWhitespace(pair[ 0 ]);
                if (!StringUtils.hasText(pairKey)) {
                    pairKey = null;
                }
                String pairValue = StringUtils.trimWhitespace(pair[ 1 ]);
                if (!StringUtils.hasText(pairValue)) {
                    pairValue = null;
                }
                if ("status".equalsIgnoreCase(pairKey)) {
                    int statusCode = getRequiredInt(pairKey, pairValue);
                    builder.setStatus(statusCode);
                    statusSet = true;
                } else if ("code".equalsIgnoreCase(pairKey)) {
                    int code = getRequiredInt(pairKey, pairValue);
                    builder.setCode(code);
                    codeSet = true;
                } else if ("msg".equalsIgnoreCase(pairKey)) {
                    builder.setMessage(pairValue);
                    msgSet = true;
                } else if ("devMsg".equalsIgnoreCase(pairKey)) {
                    builder.setDeveloperMessage(pairValue);
                    devMsgSet = true;
                } else if ("infoUrl".equalsIgnoreCase(pairKey)) {
                    builder.setMoreInfoUrl(pairValue);
                    moreInfoSet = true;
                }
            } else {
                //not a key/value pair - use heuristics to determine what value is being set:
                int val;
                if (!statusSet) {
                    val = getInt("status", trimmedVal);
                    if (val > 0) {
                        builder.setStatus(val);
                        statusSet = true;
                        continue;
                    }
                }
                if (!codeSet) {
                    val = getInt("code", trimmedVal);
                    if (val > 0) {
                        builder.setCode(val);
                        codeSet = true;
                        continue;
                    }
                }
                if (!msgSet) {
                    builder.setMessage(trimmedVal);
                    msgSet = true;
                    continue;
                }
                if (!devMsgSet) {
                    builder.setDeveloperMessage(trimmedVal);
                    devMsgSet = true;
                    continue;
                }
                if (!moreInfoSet) {
                    builder.setMoreInfoUrl(trimmedVal);
                    moreInfoSet = true;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
        }

        return builder.build();
    }


    private static int getRequiredInt(String key, String value) {
        try {
            int anInt = Integer.parseInt(value);
            return Math.max(-1, anInt);
        } catch (NumberFormatException e) {
            String msg = "Configuration element '" + key + "' requires an integer value.  The value " +
                    "specified: " + value;
            throw new IllegalArgumentException(msg, e);
        }
    }

    private static int getInt(String key, String value) {
        try {
            return getRequiredInt(key, value);
        } catch ( IllegalArgumentException iae) {
            return 0;
        }
    }
}
