package com.savdev.jaxrs.boundary;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Joiner;
import com.savdev.jaxrs.service.UserService;

/**
 */
@Path(JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT) //always starts with "/"
public class JaxRsCRUDService
{
    @Context
    UriInfo uriInfo;

    @Inject
    UserService userService;

    /**
     * @return  A 201 (Created) status code and a Location header whose value is the URI to the newly created resource.
     *          A 201 response MAY contain an ETag response header field
     *
     *          If resource already exists, return 409 Conflict
     *
     *          A 400 (Bad Request) status code is returned with error description if validation fails
     *
     *          Response body content may or may not be present:
     *          You MAY return 200 (OK) status code, with an entity describing or containing the result of the action
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(final UserDto user) throws URISyntaxException
    {
        if (user == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(UserService.USER_CANNOT_BE_NULL).build();
        }
        if (userService.exists(user.getId()))
        {
            return Response.status(Response.Status.CONFLICT).entity(UserService.USER_ALREADY_EXISTS).build();
        }

        final Validator validator = userService.validate(user);
        if (validator.isNotValid())
        {
            String commonError = Joiner.on("; ").skipNulls().join(validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(commonError).build();
        }

        final int userId = userService.create(user);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Integer.toString(userId)).build()).tag(entityTag)
                .build();
    }

    /**
     * To request data by id, url looks like: ${application_url}/crud/25
     * To return a partial response, url looks like: ${application_url}/crud/25?fields=userId,fullname,title
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
            return Response.status(Response.Status.BAD_REQUEST).entity(UserService.WRONG_ID).build();
        }
        if (!userService.exists(id))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(UserService.NOT_EXISTING_ID + id).build();
        }

        final UserDto user = userService.get(id);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        return Response.ok(user, MediaType.APPLICATION_JSON_TYPE).tag(entityTag).build();
    }

    //TODO paging, filtering, sorting
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAll()
    {
        return "Test";
    }
    //
    //    @GET
    //    @Produces(MediaType.TEXT_HTML)
    //    public String getFilteredResult()
    //    {
    //        return "Test";
    //    }
    //
    //    @PUT
    //    @Produces()
    //    public void update()
    //    {
    //
    //    }
    //
    //    @PATCH
    //    @Produces
    //    public void partialUpdate()
    //    {
    //    }
    //
    //    @DELETE
    //    @Produces
    //    public void delete()
    //    {
    //
    //    }

}
