package org.wicketstuff.rest.swagger;

import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.IResource;
import org.wicketstuff.rest.utils.mounting.PackageScanner;

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
		SwaggerResourceReference reference = new SwaggerResourceReference("testNAme", resources);
		
		mountResource("/api/doc", reference);
	}
}
