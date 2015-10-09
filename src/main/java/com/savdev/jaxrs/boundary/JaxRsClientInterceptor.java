package com.savdev.jaxrs.boundary;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;

/**
 */
@Provider
public class JaxRsClientInterceptor implements ClientRequestFilter {

    public static final String UI_DATA_KEY = "someKey";
    public static final String UI_DATA = "someData";
    public static final String NEW_COOKIE_KEY = "newTestCookie";
    public static final String COOKIE_DATA = "cookieDataFromInterceptor";

    private static ClientRequestFilter filter = new JaxRsClientInterceptor();

    private JaxRsClientInterceptor() {
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add(UI_DATA_KEY, UI_DATA);
        //you cannot set cookie here via
        //set cookie via headers:
        final List<Object> cookies = requestContext.getHeaders().get("Cookie");
        cookies.add(new Cookie(NEW_COOKIE_KEY, COOKIE_DATA));
        requestContext.getHeaders().put("Cookie", cookies);
    }

    public static ClientRequestFilter getInstance()
    {
        return filter;
    }
}
