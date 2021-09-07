package com.xd.oktopus.annotation.method;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@OktopusRequestType(HttpMethod.DELETE)
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface DeleteRequest {

    Class<?> responseType() default byte[].class;

}
