package com.savdev.jaxrs.boundary;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.Sets;

/**
 */
@ApplicationPath("/")
public class JAXRSConfiguration2TestClientInterceptor extends Application
{
    public static final String JAX_RS_BASE_ENDPOINT= "/testclient";

    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> getClasses()
    {
        return Sets.newHashSet(JaxRs2TestClientInterceptor.class);
    }
}
