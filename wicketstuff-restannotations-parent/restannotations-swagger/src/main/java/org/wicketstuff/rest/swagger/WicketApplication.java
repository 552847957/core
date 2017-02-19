package org.wicketstuff.rest.swagger;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.wicketstuff.rest.utils.mounting.PackageScanner;

import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see org.wicketstuff.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		List<IResource> resources = PackageScanner.scanPackage("org.wicketstuff.rest.swagger");
		
		Info info = new Info()
            .title("Swagger Sample App")
            .description("This is a sample server.")
            .termsOfService("http://swagger.io/terms/")
            .contact(new Contact()
                    .email("test@test.io"))
            .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
		
		Swagger swagger = new Swagger().info(info);
		
		SwaggerResourceReference reference = new SwaggerResourceReference("testName", resources, swagger);
		
		mountResource("/api/doc", reference);
	}
}
