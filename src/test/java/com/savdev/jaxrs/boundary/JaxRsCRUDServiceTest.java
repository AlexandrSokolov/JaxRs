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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.savdev.jaxrs.TestConstants;

/**
 */
@RunWith(Arquillian.class)
public class JaxRsCRUDServiceTest
{
    public static final String TEST_DATE = "test data";
    public static final int NOT_EXISTING_KEY = 999;

    @Deployment
    public static WebArchive createDeployment() throws URISyntaxException
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("tests");
        String baseDir = resourceBundle.getString("basedir");
        File[] files = Maven.resolver().loadPomFromFile(baseDir + File.separator + "pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.PROVIDED).resolve().withTransitivity().asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, "jaxrs.war")
                .addPackage(JAXRSConfiguration.class.getPackage())
                .addAsLibraries(files)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    public void testCreateSuccessful() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        final Response response = target.request().post(Entity.entity(TEST_DATE, MediaType.TEXT_HTML_TYPE));
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        Assert.assertTrue(response.getHeaderString("Location")
                .startsWith(TestConstants.HOST_PORT + TestConstants.JAX_RS_BASE_ENDPOINT
                        + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT));
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(TEST_DATE.getBytes(
                StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, response.getEntityTag());
    }

    @Test
    public void testCreateNoData() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        final Response response = target.request().post(Entity.entity("", MediaType.TEXT_HTML_TYPE));
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Assert.assertEquals(JaxRsCRUDService.DATA_CANNOT_BE_EMPTY, response.readEntity(String.class));
    }

    @Test
    public void testGetAfterCreate() throws IOException
    {
        Client client = ClientBuilder.newClient();
        final WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT);
        final Response response = target.request().post(Entity.entity(TEST_DATE, MediaType.TEXT_HTML_TYPE));
        final String newResourceUrl = response.getHeaderString("Location");
        Assert.assertTrue(newResourceUrl.startsWith(TestConstants.HOST_PORT + TestConstants.JAX_RS_BASE_ENDPOINT
                + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT));

        final Client getClient = ClientBuilder.newClient();
        final WebTarget getTarget = getClient.target(newResourceUrl);
        final Response getResponse = getTarget.request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        EntityTag entityTag = new EntityTag(org.apache.commons.codec.digest.DigestUtils.md5Hex(TEST_DATE.getBytes(
                StandardCharsets.UTF_8)));
        Assert.assertEquals(entityTag, getResponse.getEntityTag());
        Assert.assertEquals(TEST_DATE, getResponse.readEntity(String.class));
    }

    @Test
    public void testGetNotExisting() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.HOST_PORT).path(
                TestConstants.JAX_RS_BASE_ENDPOINT + JAXRSConfiguration.JAX_RS_CRUD_ENDPOINT + "/" + NOT_EXISTING_KEY);
        final Response response = target.request().get();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Assert.assertEquals(JaxRsCRUDService.NOT_EXISTING_ID + NOT_EXISTING_KEY, response.readEntity(String.class));
    }
}
