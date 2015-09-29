package com.savdev.jaxrs.boundary;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;


/**
 */
@Path(JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT) //always starts with "/"
public class JaxRsCRUDService
{
    public static final String DATA_CANNOT_BE_EMPTY = "Data cannot be null or empty";
    public static final String WRONG_ID = "Entity ID cannot be null or empty";
    public static final String NOT_EXISTING_ID = "Cannot find entity with id = ";
    private static final Map<Integer, String> cache = Maps.newConcurrentMap();

    @Context
    UriInfo uriInfo;

    /**
     * @return a 201 (Created) status code and a Location header whose value is the URI to the newly created resource.
     *          A 201 response MAY contain an ETag response header field
     *          A 400 (Bad Request) status code is returned with error description if validation fails
     *          Response body content may or may not be present:
     *          You MAY return 200 (OK) status code, with an entity describing or containing the result of the action
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.TEXT_HTML)
    public Response create(final String data) throws URISyntaxException
    {
        if (StringUtils.isEmpty(data))
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(DATA_CANNOT_BE_EMPTY).build();
        }
        final int key = new Random().nextInt(100);
        cache.put(key, data);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(data.getBytes(
                StandardCharsets.UTF_8)));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Integer.toString(key)).build()).tag(entityTag)
                .build();
    }

    /**
     * To request data by id, url looks like: ${application_url}/crud/25
     *
     * @param id
     * @return
     */
    @GET
    @Path("/{id}")
    public Response read(@PathParam("id") final int id)
    {
        if (id == 0)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_ID).build();
        }
        if (!cache.containsKey(id))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(NOT_EXISTING_ID + id).build();
        }
        final String data = cache.get(id);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(data.getBytes(
                StandardCharsets.UTF_8)));
        return Response.ok(data, MediaType.TEXT_HTML_TYPE).tag(entityTag).build();
    }

    //TODO paging, filtering, sorting
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAll()
    {
        return "Test";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getFilteredResult()
    {
        return "Test";
    }

    @PUT
    @Produces()
    public void update()
    {

    }

    @PATCH
    @Produces
    public void partialUpdate()
    {
    }

    @DELETE
    @Produces
    public void delete()
    {

    }

}
