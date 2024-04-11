package com.poc.utilities.commons.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class CustomOAuth2AuthenticationEntryPoint extends org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint {
    protected static final Log LOGGER = LogFactory.getLog(CustomOAuth2AuthenticationEntryPoint.class);
    private static final ObjectMapper oMapper = new ObjectMapper();

    private static DefaultExceptionHandler defaultExceptionHandler;
    private String typeName = OAuth2AccessToken.BEARER_TYPE;
    private String realmName = "oauth";

    static {
        LOGGER.debug("Initializing DefaultHandlerExceptionResolver");
        ExceptionTemplateProvider defaultProvider = TemplateProviders.getDefaultProvider();
        defaultExceptionHandler = new DefaultExceptionHandler(defaultProvider.getExceptionMappings());
    }

    private String extractAuthTypePrefix(String header) {
        String existing = header;
        String[] tokens = existing.split(" +");
        if (tokens.length > 1 && !tokens[0].endsWith(",")) {
            existing = StringUtils.arrayToDelimitedString(tokens, " ").substring(existing.indexOf(' ') + 1);
        }
        return existing;
    }

    @Override
    protected ResponseEntity<?> enhanceResponse(ResponseEntity<?> response, Exception exception) {
        HttpHeaders headers = response.getHeaders();
        String existing = null;
        final String authHeaderName = "WWW-Authenticate";
        if (headers.containsKey(authHeaderName)) {
            existing = extractAuthTypePrefix(headers.getFirst(authHeaderName));
        }
        StringBuilder builder = new StringBuilder();
        builder.append(typeName).append(" ");
        builder.append("realm=\"").append(realmName).append("\"");
        if (existing!=null) {
            builder.append(", ").append(existing);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.putAll(response.getHeaders());
        httpHeaders.set(authHeaderName, builder.toString());

        OAuth2Exception body = (OAuth2Exception) response.getBody();
        ExceptionHandler handler = DefaultRestExceptionHandler.findHandler(exception, defaultExceptionHandler);
        RestError error = handler.getRestError(exception);
        Map errorBody = oMapper.convertValue(body, Map.class);
        error.setCustomData(errorBody);
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(error.toMap(), httpHeaders, response.getStatusCode());
    }
}
