package org.wicketstuff.rest.utils;

import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.wicketstuff.rest.utils.http.HttpMethod;
import org.wicketstuff.rest.utils.http.HttpUtils;

/**
 * Utility class to extract and handle the information from class IResource.Attributes
 * 
 * @author andrea del bene
 *
 */
public class AttributesWrapper
{
	private final WebResponse webResponse;

	private final WebRequest webRequest;

	private final PageParameters pageParameters;

	private final HttpMethod httpMethod;
	
	private final Attributes originalAttributes;

	public AttributesWrapper(Attributes attributes)
	{
		this.webRequest = (WebRequest)attributes.getRequest();
		this.webResponse = (WebResponse)attributes.getResponse();
		this.pageParameters = attributes.getParameters();
		this.httpMethod = HttpUtils.getHttpMethod(attributes.getRequest());
		this.originalAttributes = attributes;
	}

	public Attributes getOriginalAttributes() 
	{		
		return originalAttributes;
	}

	public WebResponse getWebResponse()
	{
		return webResponse;
	}

	public WebRequest getWebRequest()
	{
		return webRequest;
	}

	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	public HttpMethod getHttpMethod()
	{
		return httpMethod;
	}
}