package org.wicketstuff.rest.hateaos.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface HypermediaParameter
{
    int parameterIndex();
    String propertyExpression();
}
