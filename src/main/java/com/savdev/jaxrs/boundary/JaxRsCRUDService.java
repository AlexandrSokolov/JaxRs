package com.savdev.jaxrs.boundary;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 */
@Path(JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT) //always starts with "/"
public class JaxRsCRUDService
{
    public static final String USER_CANNOT_BE_NULL = "User cannot be null";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String NEW_USER_CANNOT_HAVE_ID_FIELD = "New user cannot have an id field set";
    public static final String USER_NAME_CANNOT_BE_EMPTY = "User name cannot be null or empty";
    public static final String USER_LASTNAME_CANNOT_BE_EMPTY = "User lastname cannot be null or empty";
    public static final String USER_AGE_IS_SMALL = "User age is too small";
    public static final String WRONG_ID = "Entity ID cannot be null or empty";
    public static final String NOT_EXISTING_ID = "Cannot find entity with id = ";
    private static final Map<Integer, UserDto> cache = Maps.newConcurrentMap();

    @Context
    UriInfo uriInfo;

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
            return Response.status(Response.Status.BAD_REQUEST).entity(USER_CANNOT_BE_NULL).build();
        }
        if (user.getId() != 0 && cache.containsKey(user.getId()))
        {
            return Response.status(Response.Status.CONFLICT).entity(USER_ALREADY_EXISTS).build();
        }

        Validator validator = validate(user);
        if (validator.isNotValid())
        {
            String commonError = Joiner.on("; ").skipNulls().join(validator.getErrors());
            return Response.status(Response.Status.BAD_REQUEST).entity(commonError).build();
        }
        final int key = new Random().nextInt(100);
        cache.put(key, user);

        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Integer.toString(key)).build()).tag(entityTag)
                .build();
    }

    private Validator validate(UserDto user)
    {
        if (user == null)
        {
            throw new IllegalArgumentException(USER_CANNOT_BE_NULL);
        }

        final Validator validator = Validator.newInstance();
        boolean isValid = true;
        if (user.getId() != 0 )
        {
            isValid = false;
            validator.addError(NEW_USER_CANNOT_HAVE_ID_FIELD);
        }

        if (StringUtils.isEmpty(user.getName()))
        {
            isValid = false;
            validator.addError(USER_NAME_CANNOT_BE_EMPTY);
        }

        if (StringUtils.isEmpty(user.getLastName()))
        {
            isValid = false;
            validator.addError(USER_LASTNAME_CANNOT_BE_EMPTY);
        }

        if (user.getAge() <= 18)
        {
            isValid = false;
            validator.addError(USER_AGE_IS_SMALL);
        }

        if (isValid)
        {
            validator.markAsValid();
        }

        return validator;
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
            return Response.status(Response.Status.BAD_REQUEST).entity(WRONG_ID).build();
        }
        if (!cache.containsKey(id))
        {
            return Response.status(Response.Status.NOT_FOUND).entity(NOT_EXISTING_ID + id).build();
        }

        final UserDto user = cache.get(id);
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        return Response.ok(user, MediaType.APPLICATION_JSON_TYPE).tag(entityTag).build();
    }

    //TODO paging, filtering, sorting
    //    @GET
    //    @Produces(MediaType.TEXT_HTML)
    //    public String getAll()
    //    {
    //        return "Test";
    //    }
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
