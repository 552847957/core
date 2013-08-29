package org.wicketstuff.rest.heteaos.annotations;

import org.wicketstuff.rest.utils.http.HttpMethod;

public @interface EntityOperation {
	Class<?> clazz();
	HttpMethod httpMethod();
	String fieldName() default "";
}
