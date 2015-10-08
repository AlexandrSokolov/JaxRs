package com.savdev.jaxrs.boundary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.savdev.jaxrs.TestConstants;

/**
 * Purpose of this class show how to test access to static resources, for example: Index.html
 */
@RunWith(Arquillian.class)
public class JaxRsWithWebResourceTest
{
    @Deployment
    public static WebArchive createDeployment() throws URISyntaxException
    {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "jaxrs.war")
                .addAsWebResource(new File(JaxRsWithWebResourceTest.class.getClassLoader()
                        .getResource("Index.html").getFile()));
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    @RunAsClient
    public void testGetIndexHtml() throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(TestConstants.HOST_PORT)
                .path(TestConstants.JAX_RS_BASE_ENDPOINT + "/Index.html");
        Response response  =     webTarget.request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        final InputStream expected = JaxRsWithWebResourceTest.class.getResourceAsStream("/Index.html");
        Assert.assertEquals(IOUtils.toString(expected, StandardCharsets.UTF_8), response.readEntity(String.class));
    }

}
