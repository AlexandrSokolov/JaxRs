package com.savdev.jaxrs.boundary;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
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

/** We test here, that when JaxRs Client is invoked,
 * the WebFilterPassDataFromUiLayerToBusinessComponent web filter adds some data from UI layer into
 * the ThreadLocalContextHolder
 * And when business component is invoked, this data is accessible there inside of the PojoBeanWithAccessToUi class
 */
@RunWith(Arquillian.class)
public class JaxRsClientInterceptorTest {

    @Deployment
    public static WebArchive createDeployment() throws URISyntaxException
    {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("tests");
        String baseDir = resourceBundle.getString("basedir");
        File[] files = Maven.resolver().loadPomFromFile(baseDir + File.separator + "pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.PROVIDED).resolve().withTransitivity().asFile();
        WebArchive war = ShrinkWrap.create(WebArchive.class, TestConstants.JAX_RS_BASE_ENDPOINT + ".war")
                .addClasses(JaxRs2TestClientInterceptor.class, JAXRSConfiguration2TestClientInterceptor.class)
                .addAsLibraries(files)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;
    }

    /**
     * Now we force to attache header in AOP way when use JAX RS Client API.
     * This header attached in JaxRsClientInterceptor
     * and returned by rest service as an entity
     * @throws IOException
     */
    @Test
    @RunAsClient
    public void testHeaderIsAttachedWhenJaxRsClientApiIsUsed() throws IOException
    {
        //register interceptor that will use data from ThreadLocalContextHolder to attach it into request
        Client client = ClientBuilder.newClient().register(JaxRsClientInterceptor.getInstance());
        Response response = client.target(TestConstants.HOST_PORT)
                .path(TestConstants.JAX_RS_BASE_ENDPOINT
                        + JAXRSConfiguration2TestClientInterceptor.JAX_RS_BASE_ENDPOINT)
                .request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertEquals(JaxRsClientInterceptor.UI_DATA, response.readEntity(String.class));
    }
}
