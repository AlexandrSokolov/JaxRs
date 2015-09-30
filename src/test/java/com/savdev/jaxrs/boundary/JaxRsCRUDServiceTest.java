package com.savdev.jaxrs.boundary;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
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

import com.savdev.jaxrs.TestConstants;
import com.savdev.jaxrs.service.UserService;

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
        WebArchive war = ShrinkWrap.create(WebArchive.class, "jaxrs.war")
                .addPackages(true, Filters.exclude(".*Test.*"), JAXRSConfiguration.class.getPackage(),
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
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        UserDto user = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.not.existing.user.json"),
                        UserDto.class);
        final Response response = target.request().post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(response.getHeaderString("Location")
                .startsWith(TestConstants.HOST_PORT + TestConstants.JAX_RS_BASE_ENDPOINT
                        + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT));
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, response.getEntityTag());
    }

    @Test
    @RunAsClient
    public void testCreateWithExistingId() throws IOException
    {
        //Response.Status.CONFLICT 409
    }

    @Test
    @RunAsClient
    public void testCreateWithNotExistingId() throws IOException
    {
        //Response.Status.BAD_REQUEST 400
    }

    @Test
    @RunAsClient
    public void testCreateWrongUser() throws IOException
    {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        UserDto user = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/wrong.not.existing.user.json"),
                        UserDto.class);
        final Response response = target.request().post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_LASTNAME_CANNOT_BE_EMPTY, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testCreateNoData() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        final Response response = target.request().post(Entity.entity("", MediaType.APPLICATION_JSON_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.USER_CANNOT_BE_NULL, response.readEntity(String.class));
    }

    @Test
    @RunAsClient
    public void testGetAfterCreate() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        UserDto user = new ObjectMapper()
                .readValue(JaxRsCRUDServiceTest.class.getResourceAsStream("/data/correct.not.existing.user.json"),
                        UserDto.class);
        final Response response = target.request().post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
        final String newResourceUrl = response.getHeaderString("Location");
        Assert.assertNotNull(newResourceUrl);

        final Client getClient = ClientBuilder.newClient();
        final WebTarget getTarget = getClient.target(newResourceUrl);
        final Response getResponse = getTarget.request(MediaType.APPLICATION_JSON_TYPE).get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(user.toString().getBytes(
                StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, getResponse.getEntityTag());
        Assert.assertEquals(user, getResponse.readEntity(UserDto.class));
    }

    @Test
    @RunAsClient
    public void testGetNotExisting() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT + "/" + NOT_EXISTING_KEY);
        final Response response = target.request().get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals(UserService.NOT_EXISTING_ID + NOT_EXISTING_KEY, response.readEntity(String.class));
    }
}
