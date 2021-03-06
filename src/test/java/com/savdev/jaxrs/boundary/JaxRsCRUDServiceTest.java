package com.savdev.jaxrs.boundary;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;
import com.savdev.jaxrs.TestConstants;
import com.savdev.jaxrs.service.UserService;
import com.savdev.jaxrs.service.UserServiceMockUserAlreadyExists;

/**
 */
@RunWith(Arquillian.class)
public class JaxRsCRUDServiceTest
{
    private static final int NOT_EXISTING_KEY = 999;

    @Deployment
    public static WebArchive createDeployment() throws URISyntaxException
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("tests");
        String baseDir = resourceBundle.getString("basedir");
        File[] files = Maven.resolver().loadPomFromFile(baseDir + File.separator + "pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.PROVIDED).resolve().withTransitivity().asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, TestConstants.JAX_RS_BASE_ENDPOINT+ ".war")
                .addPackages(true, Filters.exclude(".*Test.*|.*Mock.*"), JAXRSConfiguration.class.getPackage(),
                        UserService.class.getPackage())
                .addAsLibraries(files)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    @RunAsClient
    public void testCreateSuccessful() throws IOException
    {
        UserDto user = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.not.existing.user.json"),
                        UserDto.class);
        final Response response = createTargetForRealUserService().request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(response.getHeaderString("Location")
                .startsWith(TestConstants.HOST_PORT + "/" + TestConstants.JAX_RS_BASE_ENDPOINT
                        + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT));
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, response.getEntityTag());
    }

    @Test
    @RunAsClient
    public void testCreateWrongUser() throws IOException
    {
        UserDto user = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/wrong.not.existing.user.json"),
                        UserDto.class);
        final Response response = createTargetForRealUserService().request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_LASTNAME_CANNOT_BE_EMPTY, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testCreateNoData() throws IOException
    {
        final Response response = createTargetForRealUserService().request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_CANNOT_BE_NULL, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testCreateWithIdConfigured() throws IOException
    {
        UserDto userDto = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.not.existing.user.json"),
                        UserDto.class);
        userDto.setId(UserServiceMockUserAlreadyExists.EXISTING_KEY);
        final Response response = createTargetForRealUserService().request().post(
                Entity.entity(userDto, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.NEW_USER_CANNOT_HAVE_ID_FIELD, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testGetNotExisting() throws IOException
    {
        final Response response = createTargetForRealUserService("/" + NOT_EXISTING_KEY).request().get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.CANNOT_FIND_ENTITY + NOT_EXISTING_KEY, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testUpdateNotExisting() throws IOException
    {
        UserDto userDto = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.user.tobe.updated.json"),
                        UserDto.class);
        final Response response = createTargetForRealUserService("/" + NOT_EXISTING_KEY).request()
                .put(Entity.entity(userDto, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.CANNOT_FIND_ENTITY + NOT_EXISTING_KEY, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testDeleteNotExisting() throws IOException
    {
        final Response response = createTargetForRealUserService("/" + NOT_EXISTING_KEY).request()
                .delete();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.CANNOT_FIND_ENTITY + NOT_EXISTING_KEY, response.readEntity(String.class));
    }

    @Deployment(name = TestConstants.USER_EXISTS_BASE_ENDPOINT)
    public static WebArchive createMockUserAlreadyExistDeployment() throws URISyntaxException
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("tests");
        String baseDir = resourceBundle.getString("basedir");
        File[] files = Maven.resolver().loadPomFromFile(baseDir + File.separator + "pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.PROVIDED).resolve().withTransitivity().asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, TestConstants.USER_EXISTS_BASE_ENDPOINT + ".war")
                .addPackages(true, Filters.exclude(".*Test.*"), JAXRSConfiguration.class.getPackage(),
                        UserService.class.getPackage())
                .addClass(UserServiceMockUserAlreadyExists.class)
                .addAsLibraries(files)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;
    }

    /**
     * When we try to create a user, and set ID, and user with such ID already creats, it's forbidden,
     * you get CONFLICT responce
     *
     * @throws IOException
     */
    @Test
    @RunAsClient
    public void testCreateWithExistingId() throws IOException
    {
        //Response.Status.CONFLICT 409
        UserDto user = new UserDto();
        user.setId(UserServiceMockUserAlreadyExists.EXISTING_KEY);
        final Response response = createTargetForMockUserAlreadyExists().request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_ALREADY_EXISTS, response.readEntity(String.class));

    }

    @Test
    @RunAsClient
    public void testGet() throws IOException
    {
        final Response getResponse = createTargetForMockUserAlreadyExists(
                "/" + UserServiceMockUserAlreadyExists.EXISTING_KEY).request(MediaType.APPLICATION_JSON_TYPE).get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils
                .md5Hex(UserServiceMockUserAlreadyExists.existingUser.toString().getBytes(
                        StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, getResponse.getEntityTag());
        Assert.assertEquals(UserServiceMockUserAlreadyExists.existingUser, getResponse.readEntity(UserDto.class));
    }

    @Test
    @RunAsClient
    public void testGetAll() throws IOException
    {
        final Response getResponse = createTargetForMockUserAlreadyExists().request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        List<UserDto> expectedResult = Lists.newArrayList(UserServiceMockUserAlreadyExists.userDto1,
                UserServiceMockUserAlreadyExists.userDto2, UserServiceMockUserAlreadyExists.userDto3);
        //if you get getResponse.readEntity(List.class); you'll get a list of Maps, but not objects
        List<UserDto> actual = getResponse.readEntity(new GenericType<List<UserDto>>()
        {
        });
        MatcherAssert.assertThat(actual, IsIterableContainingInOrder.contains(expectedResult.toArray()));
    }

    @Test
    @RunAsClient
    public void testGetPagination() throws IOException
    {
        final Response getResponse = createTargetForMockUserAlreadyExists()
                .queryParam("offset", UserServiceMockUserAlreadyExists.offset)
                .queryParam("maxResults", UserServiceMockUserAlreadyExists.maxResults)
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        ListResource listResource = getResponse.readEntity(ListResource.class);
        Assert.assertNotNull(listResource);
        Assert.assertEquals(UserServiceMockUserAlreadyExists.offset, listResource.getOffset());
        Assert.assertEquals(UserServiceMockUserAlreadyExists.maxResults, listResource.getMaxResult());
        Assert.assertEquals(UserServiceMockUserAlreadyExists.numberOfPages, listResource.getNumberOfPages());
        Assert.assertEquals(UserServiceMockUserAlreadyExists.maxResults, listResource.getItems().size());
    }

    @Test
    @RunAsClient
    public void testGetPaginationWithoutOffset() throws IOException
    {
        final Response response = createTargetForMockUserAlreadyExists()
                .queryParam("maxResults", UserServiceMockUserAlreadyExists.maxResults)
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(ListResource.OFFSET_AND_MAX_RESULT_MUST_EXISTS, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testGetPaginationWithoutMaxResult() throws IOException
    {
        final Response response = createTargetForMockUserAlreadyExists()
                .queryParam("offset", UserServiceMockUserAlreadyExists.offset)
                .request(MediaType.APPLICATION_JSON_TYPE).get();
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(ListResource.OFFSET_AND_MAX_RESULT_MUST_EXISTS, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testUpdateSuccessful() throws IOException
    {
        UserDto userDto = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.user.tobe.updated.json"),
                        UserDto.class);
        final Response response = createTargetForMockUserAlreadyExists(
                "/" + UserServiceMockUserAlreadyExists.EXISTING_KEY).request()
                .put(Entity.entity(userDto, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    @RunAsClient
    public void testUpdateWrongData() throws IOException
    {
        UserDto userDto = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/wrong.not.existing.user.json"),
                        UserDto.class);
        final Response response = createTargetForMockUserAlreadyExists(
                "/" + UserServiceMockUserAlreadyExists.EXISTING_KEY).request()
                .put(Entity.entity(userDto, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_LASTNAME_CANNOT_BE_EMPTY, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testUpdateNullableUser() throws IOException
    {
        final Response response = createTargetForMockUserAlreadyExists(
                "/" + UserServiceMockUserAlreadyExists.EXISTING_KEY).request()
                .put(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_CANNOT_BE_NULL, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testDeleteSuccessful() throws IOException
    {
        final Response response = createTargetForMockUserAlreadyExists(
                "/" + UserServiceMockUserAlreadyExists.EXISTING_KEY).request()
                .delete();
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    private WebTarget createTargetForRealUserService()
    {
        return createTargetForRealUserService("");
    }

    private WebTarget createTargetForRealUserService(final String prefix)
    {
        Client client = ClientBuilder.newClient();
        return client.target(TestConstants.HOST_PORT)
                .path(TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT + prefix);
    }

    /**
     * @return WebTarget for UserService mock that always returns true when exists() is invoked
     */
    private WebTarget createTargetForMockUserAlreadyExists()
    {
        return createTargetForMockUserAlreadyExists("");
    }

    /**
     * @return WebTarget for UserService mock that always returns true when exists() is invoked
     */
    private WebTarget createTargetForMockUserAlreadyExists(final String prefix)
    {
        Client client = ClientBuilder.newClient();
        return client.target(TestConstants.HOST_PORT)
                .path(TestConstants.USER_EXISTS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT + prefix);
    }
}
