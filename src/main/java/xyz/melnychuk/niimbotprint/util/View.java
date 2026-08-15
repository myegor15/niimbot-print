package xyz.melnychuk.niimbotprint.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface View {

    String fxml();

    int width() default -1;

    int height() default -1;

    String[] stylesheets() default {};
}