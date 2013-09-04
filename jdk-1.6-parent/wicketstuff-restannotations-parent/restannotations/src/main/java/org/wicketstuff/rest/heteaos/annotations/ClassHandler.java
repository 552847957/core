package org.wicketstuff.rest.heteaos.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassHandler {
	Class<?> value();
}
