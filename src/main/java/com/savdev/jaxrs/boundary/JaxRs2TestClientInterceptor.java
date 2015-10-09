package com.savdev.jaxrs.boundary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 */
@Path(JAXRSConfiguration2TestClientInterceptor.JAX_RS_BASE_ENDPOINT)
public class JaxRs2TestClientInterceptor {
    @GET
    public String get(@Context HttpHeaders headers)
    {
        return headers.getHeaderString(JaxRsClientInterceptor.UI_DATA_KEY);
    }
}
