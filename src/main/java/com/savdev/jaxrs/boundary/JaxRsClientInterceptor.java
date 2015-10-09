package com.savdev.jaxrs.boundary;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

/**
 */
@Provider
public class JaxRsClientInterceptor implements ClientRequestFilter {

    public static final String UI_DATA_KEY = "someKey";
    public static final String UI_DATA = "someData";

    private static ClientRequestFilter filter = new JaxRsClientInterceptor();

    private JaxRsClientInterceptor() {
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add(UI_DATA_KEY, UI_DATA);
    }

    public static ClientRequestFilter getInstance()
    {
        return filter;
    }
}
