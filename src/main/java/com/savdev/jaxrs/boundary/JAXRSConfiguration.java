package com.savdev.jaxrs.boundary;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 */
public class JAXRSConfiguration extends Application
{
    @Override
    @SuppressWarnings("unchecked")
    public Set<Class<?>> getClasses()
    {
        return Collections.emptySet();
    }
}
