package org.wicketstuff.rest.utils.patterns.compositechain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompoundAction implements IAction 
{
	
	private List<IAction> actions = new ArrayList<IAction>();
	
	@Override
	public boolean executeAction(Map context) 
	{
		
		for(IAction action : actions)
		{
			if(!action.executeAction(context))
				return false;
		}
		
		return true;
	}
	
	public void add(IAction action)
	{
		actions.add(action);
	}
	
	public void remove(IAction action)
	{
		actions.remove(action);
	}
}
