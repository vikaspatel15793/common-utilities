package com.poc.utilities.commons.error;

public class TemplateProviders {
    public static ExceptionTemplateProvider getDefaultProvider() {
        return new FileBaseTemplateProvider();
    }
}
