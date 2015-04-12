package ams10961.siwt.rest.v1;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * build a test archive to be deployed and tested, offer utility methods
 */

@ArquillianSuiteDeployment
public class RestTestBase {

	protected static final String INVALID_AUTHORIZATION = UUID.randomUUID().toString().replaceAll("-", "");
	protected static final String INVALID_UUID = UUID.randomUUID().toString().replaceAll("-", "");
	protected static final String INVALID_AUTHENTICATION = UUID.randomUUID().toString().replaceAll("-", "");

	final static Logger logger = LoggerFactory.getLogger(RestTestBase.class);

	// e.g. "http://localhost:8080/test"
	@ArquillianResource
	URL deploymentUrl;

	/*
	 * create the deployment archive, only once
	 */
	@Deployment(testable = true)
	public static Archive<?> createTestArchive() {

		// create the WAR archive for testing
		WebArchive testWar = ShrinkWrap.create(WebArchive.class, TestConstants.TEST_WAR);

		// include application classes
		testWar.addPackages(true, "ams10961.siwt");

		// resolve extension libraries
		MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom(
				"pom.xml");

		testWar.addAsLibraries(resolver.artifact("org.json:json:20090211").resolveAsFiles());
		// this is necessary to avoid a codec exception
		testWar.addAsLibraries(resolver.artifact("org.apache.httpcomponents:httpclient:4.3.3").resolveAsFiles());

		// JPA persistence files
		testWar.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml");

		// configuration
		testWar.addAsResource("log4j.properties");

		// needed for CDI
		testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		// Deploy test datasource
		testWar.addAsWebInfResource("test-ds.xml");
		
		// SQL import
		testWar.addAsResource("import.sql", "import.sql");

		// include test HTML pages
		testWar.merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class).importDirectory("src/main/webapp")
				.as(GenericArchive.class), "/", Filters.include(".*\\.html$"));

		// log contents of WAR
		logger.debug(testWar.toString(Formatters.VERBOSE));

		// ready
		return testWar;
	}

	/*
	 * HTTP-only client - for rejection testing
	 */
	protected WebTarget getNonSecureClient() {

		logger.debug("deploymentUrl:>" + deploymentUrl.toString() + "<");

		// Construct a path to the test resource
		StringBuilder testPath = new StringBuilder();
		testPath.append(deploymentUrl.toString());
		testPath.append(JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1));

		// Generate the client
		WebTarget testClient = ClientBuilder.newBuilder().build().target(testPath.toString());

		return testClient;
	}

	/*
	 * TODO: query dynamically for HTTPS port. 
	 */
	protected WebTarget getSecureClient() {

		URL secureDeploymentUrl = null;

		// TODO: work out how to avoid port and protocol hard-coding
		// deployment Url doesn't give secure version
		StringBuilder sb = new StringBuilder();
		sb.append("https://");
		sb.append(deploymentUrl.getHost());
		sb.append(":");
		sb.append(TestConstants.JBOSS_TESTING_SECURE_PORT);
		sb.append(deploymentUrl.getPath());
		try {
			secureDeploymentUrl = new URL(sb.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.debug("deploymentUrl:>" + deploymentUrl.toString() + "<");
		logger.debug("secureDeploymentUrl:>" + secureDeploymentUrl.toString() + "<");

		SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
		SSLContext sslContext = null;

		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new javax.net.ssl.TrustManager[] { secureRestClientTrustManager }, null);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Construct a path to the test resource
		StringBuilder testPath = new StringBuilder();
		testPath.append(secureDeploymentUrl.toString());
		testPath.append(JaxRsActivator.class.getAnnotation(ApplicationPath.class).value().substring(1));

		// Generate the client
		WebTarget testClient = ClientBuilder.newBuilder().hostnameVerifier(getHostnameVerifier())
				.sslContext(sslContext).build().target(testPath.toString());

		return testClient;
	}

	private HostnameVerifier getHostnameVerifier() {
		return new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				return true;
			}
		};
	}

	private class SecureRestClientTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		/*
		 * public boolean isClientTrusted(X509Certificate[] arg0) { return true;
		 * }
		 * 
		 * public boolean isServerTrusted(X509Certificate[] arg0) { return true;
		 * }
		 */
	}

}
