package org.wicketstuff.rest.swagger;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class SwaggerResourceReferenceTest
{
	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}
	
	@Test
	public void testName() throws Exception
	{
		tester.getRequest().setMethod("GET");
		tester.executeUrl("./api/doc");
		
		System.out.println(tester.getLastResponseAsString());
	}
}
