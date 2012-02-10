package jp.vmi.indylisp.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * This annotation is effective *ONLY* in GlobalConverters, StringWrapper, and NumberWrapper.
 */
@Retention(RUNTIME)
@Target({ METHOD })
public @interface GlobalConverter {
}
