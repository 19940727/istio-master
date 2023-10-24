package istio.example.master.client;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CloudNativeExchange {

    String service();

    String namespace() default "";

    String cluster() default "";
}
