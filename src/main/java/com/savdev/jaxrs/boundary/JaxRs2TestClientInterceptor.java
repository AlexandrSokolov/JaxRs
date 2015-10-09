package com.savdev.jaxrs.boundary;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 */
@Path(JAXRSConfiguration2TestClientInterceptor.JAX_RS_BASE_ENDPOINT)
public class JaxRs2TestClientInterceptor {

    public static final String INTERNAL_COOKIE_NAME = "inernalCoookie";

    @GET
    public String get(@Context final HttpHeaders headers,
            @CookieParam(JaxRsClientInterceptor.NEW_COOKIE_KEY) final String cookieFromInterceptor,
            @CookieParam(INTERNAL_COOKIE_NAME) final String cookieFromOriginalRequest)
    {
        final String result = "Header: '" + headers.getHeaderString(JaxRsClientInterceptor.UI_DATA_KEY) + "'"
            + "; cookieFromInterceptor = '" + cookieFromInterceptor + "'"
            + "; cookieFromRequest = '" + cookieFromOriginalRequest + "'";
        return result;
    }
}
