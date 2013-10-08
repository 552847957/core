package org.wicketstuff.rest.utils.patterns.compositechain;

import java.util.Map;

import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.http.WebResponse;
import org.wicketstuff.rest.annotations.AuthorizeInvocation;
import org.wicketstuff.rest.resource.MethodMappingInfo;
import org.wicketstuff.rest.utils.AttributesWrapper;

public class AuthorizationCheckAction implements IAction 
{

	/** Role-checking strategy. */
	private final IRoleCheckingStrategy roleCheckingStrategy;

	
	
	public AuthorizationCheckAction(IRoleCheckingStrategy roleCheckingStrategy) 
	{
		this.roleCheckingStrategy = roleCheckingStrategy;
	}

	@Override
	public boolean executeAction(Map context) 
	{
		AttributesWrapper attributesWrapper = (AttributesWrapper) context.get("attributesWrapper");
		WebResponse response = attributesWrapper.getWebResponse();
		MethodMappingInfo mappedMethod = (MethodMappingInfo) context.get("mappedMethod");
		
		//check if user is authorized to invoke the method
		if (!isUserAuthorized(mappedMethod))
		{
			response.sendError(401, "User is not allowed to use this resource.");
			return false;
		}
		
		return false;
	}
	
	/**
	 * Check if user is allowed to run a method annotated with {@link AuthorizeInvocation}
	 * 
	 * @param mappedMethod
	 * 		the target method
	 * @return
	 * 		true if user is allowed, else otherwise
	 */
	private boolean isUserAuthorized(MethodMappingInfo mappedMethod)
	{
		if (!hasAny(mappedMethod.getRoles()))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Utility method to check that the user owns one of the roles provided in input.
	 * 
	 * @param roles
	 *            the checked roles.
	 * @return true if the user owns one of roles in input, false otherwise.
	 */
	protected final boolean hasAny(Roles roles)
	{
		if (roles.isEmpty())
		{
			return true;
		}
		else
		{
			return roleCheckingStrategy.hasAnyRole(roles);
		}
	}

}
