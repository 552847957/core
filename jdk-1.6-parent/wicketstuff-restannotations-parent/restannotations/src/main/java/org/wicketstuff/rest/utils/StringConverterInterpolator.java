package org.wicketstuff.rest.utils;

import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;

public class StringConverterInterpolator extends MapVariableInterpolator{

	public StringConverterInterpolator(String string, Map<?, ?> variables,
			boolean exceptionOnNullVarValue) {
		super(string, variables, exceptionOnNullVarValue);
	}
	
	@Override
	protected String getValue(String variableName)
	{
		Object value = super.getValue(variableName);
		if (value == null)
		{
			return null;
		}
		else if (value instanceof String)
		{
			// small optimization - no need to bother with conversion
			// for String vars, e.g. {label}
			return (String)value;
		}
		else
		{
			IConverter converter = getConverter(value.getClass());
			if (converter == null)
			{
				return Strings.toString(value);
			}
			else
			{
				return converter.convertToString(value, getLocale());
			}
		}
	}

	private Locale getLocale() {
		return Session.get().getLocale();
	}

	private IConverter getConverter(Class<? extends Object> clazz) {
		return Application.get().getConverterLocator().getConverter(clazz);
	}

}
