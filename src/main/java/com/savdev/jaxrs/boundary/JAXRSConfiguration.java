package com.savdev.jaxrs.boundary;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.Sets;

/**
 */
@ApplicationPath("/")
public class JAXRSConfiguration extends Application
{
    public static final String JAX_RS_CRUD_ENDPOINT = "/crud";
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> getClasses()
    {
        return Sets.newHashSet(JaxRsCRUDService.class);
    }
}
