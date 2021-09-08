package io.github.amaizeing.oktopus.annotation.method;

import org.atteo.classindex.IndexAnnotated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IndexAnnotated
@OktopusRequestType(HttpMethod.PUT)
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Put {

    Class<?> onSuccess() default byte[].class;

    Class<?> onFailure() default byte[].class;

}
