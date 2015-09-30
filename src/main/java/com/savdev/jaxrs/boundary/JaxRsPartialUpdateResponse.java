package com.savdev.jaxrs.boundary;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * TODO
 * http://stackoverflow.com/questions/9314735/how-to-return-a-partial-json-response-using-java
 * http://javadeveloperjournal.blogspot.ru/2012/09/returning-partial-response-in-jax-rs_1.html
 */
@Path(JAXRSConfiguration.JAX_RS_PARTIAL_ENDPOINT) //always starts with "/"
public class JaxRsPartialUpdateResponse
{
    @PATCH
    @Produces
    public Response partialUpdate()
    {
        //TODO
        //should return a whole entity after update if compare it with PUT
        return null;
    }

    @GET
    @Path("/{id}")
    public Response read(@PathParam("id") final int id,  @QueryParam("fields") final String fields)
    {
        //TODO
        return null;
    }
}
