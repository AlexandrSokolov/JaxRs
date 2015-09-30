package com.savdev.jaxrs.boundary;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
     * @return A 201 (Created) status code and a Location header whose value is the URI to the newly created resource.
     * A 201 response MAY contain an ETag response header field
     * <p/>
     * If resource already exists, return 409 Conflict
     * <p/>
     * A 400 (Bad Request) status code is returned with error description if validation fails
     * <p/>
     * Response body content may or may not be present:
     * You MAY return 200 (OK) status code, with an entity describing or containing the result of the action
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

        final Validator validator = userService.validate4Create(user);
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
        if (!userService.exists(id))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(UserService.CANNOT_FIND_ENTITY + id).build();
        }

        final UserDto user = userService.get(id);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        return Response.ok(user, MediaType.APPLICATION_JSON_TYPE).tag(entityTag).build();
    }

    //TODO paging, filtering, sorting

    /**
     * You may get when return List:
     * MessageBodyWriter not found for media type=application/json, type=class java.util.Arrays$ArrayList,
     * genericType=class java.util.Arrays$ArrayList
     * <p/>
     * In this case solution is:
     * GenericEntity<List<UserDto>> list = new GenericEntity<List<UserDto>>(workshops) {};
     * and return it
     * <p/>
     * In Jax Rs Client API you can get it:
     * List<UserDto> actual = response.readEntity(new GenericType<List<UserDto>>() {});
     *
     * @return
     */
    @GET
    public Response getListOfItems(@QueryParam("offset") final int offset,
            @QueryParam("maxResults") final int maxResults)
    {
        if (offset == 0 && maxResults == 0)
        {
            return Response.ok(userService.getAll(), MediaType.APPLICATION_JSON_TYPE).build();
        }
        else
        {
            if (offset == 0 || maxResults == 0)
            {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ListResource.OFFSET_AND_MAX_RESULT_MUST_EXISTS).build();
            }

            List<UserDto> items = userService.getAll(offset, maxResults);
            ListResource listResource = new ListResource();
            listResource.setItems(items);
            listResource.setMaxResult(maxResults);
            listResource.setOffset(offset);
            listResource.setNumberOfPages(userService.numberOfPages(maxResults));
            return Response.ok(listResource, MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * See: http://stackoverflow.com/questions/797834/should-a-restful-put-operation-return-something
     *
     * HTTP status code 204 resource updated successfully
     * HTTP status code 200 OK for a successful PUT of an update to an existing resource. No response body needed.
     *              (Per Section 9.6, 204 No Content is even more appropriate.)
     * HTTP status code 201 Created for a successful PUT of a new resource, with URIs and metadata of
     *              the new resource echoed in the response body. (RFC 2616 Section 10.2.2)
     * HTTP status code 409 Conflict for a PUT that is unsuccessful due to a 3rd-party modification, with a
     *              list of differences between the attempted update and the current resource in the response body.
     *              (RFC 2616 Section 10.4.10)
     *              Note: The semantics of the PUT method is to ignore whatever current state the resource is in,
     *              therefore to return a 409 conflict for a PUT that is unsuccessful due
     *              to a 3rd party modification only makes sense if the request is conditional
     * HTTP status code 400 Bad Request for an unsuccessful PUT, with natural-language text (such as English)
     *              in the response body that explains why the PUT failed. (RFC 2616 Section 10.4)
     *
     * @param id
     * @param user
     * @return
     */
    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") final int id, final UserDto user)
    {
        if (user == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(UserService.USER_CANNOT_BE_NULL).build();
        }
        if (!userService.exists(user.getId()))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(UserService.CANNOT_FIND_ENTITY + id).build();
        }

        final Validator validator = userService.validate4Update(user);
        if (validator.isNotValid())
        {
            String commonError = Joiner.on("; ").skipNulls().join(validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(commonError).build();
        }
        userService.update(id, user);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * return a 204 (successful, no content)
     * returns a 404 (not found) on subsequent calls
     * See http://stackoverflow.com/questions/6439416/deleting-a-resource-using-http-delete
     *
     * As HTTP requests in a stateless system should be independent, the results of one request should not be
     *  dependent on a previous request. Consider what should happen if two users did a DELETE on the same resource
     *  simultaneously. It makes sense for the second request to get a 404.
     *  The same should be true if one user makes two requests.
     *
     * I am guessing that having DELETE return two different responses does not feel idempotent to you.
     * I find it useful to think of idempotent requests as leaving the system in the same state, not necessarily
     * having the same response. So regardless of whether you DELETE an existing resource, or attempt to DELETE a
     * resource that does not exist, the server resource state is the same.
     *
     * ...But in practice, implementing DELETE as an idempotent operation (same response on subsequent calls)
     *  requires the server to keep track of all deleted resources. Otherwise, it can return a 404 (Not Found).
     *
     * @param id
     */
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") final int id)
    {
        if (!userService.exists(id))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(UserService.CANNOT_FIND_ENTITY + id).build();
        }
        userService.delete(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
