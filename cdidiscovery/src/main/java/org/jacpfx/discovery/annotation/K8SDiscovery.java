package org.jacpfx.discovery.annotation;

/**
 * Created by amo on 18.05.17.
 */
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD, TYPE })
public @interface K8SDiscovery {
    String user() default "";
    String pwd() default "";
    String api_token() default "";
    String master_url() default "https://kubernetes.default.svc";
    String namespace() default "";
}
