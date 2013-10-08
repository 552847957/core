package org.wicketstuff.rest.utils.http;

public class HttpResult 
{
	private final int httpCode;
	private final String message;
	
	public static final int HTTP_OK = 200;
	
	public HttpResult(int httpCode, String message) 
	{
		this.httpCode = httpCode;
		this.message = message;
	}

	public int getHttpCode() 
	{
		return httpCode;
	}

	public String getMessage() 
	{
		return message;
	}

	public boolean isSuccessful() {
		return httpCode < 300;
	}
}
