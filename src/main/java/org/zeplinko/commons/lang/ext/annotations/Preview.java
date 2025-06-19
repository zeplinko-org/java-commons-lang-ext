package org.zeplinko.commons.lang.ext.annotations;

import java.lang.annotation.*;

/**
 * Marks a program element—class, method, constructor, field, or annotation
 * type—as a <em>preview</em> feature of this library.
 *
 * <p>
 * Preview APIs are still under active development. They may change in behavior,
 * signature, or even be removed entirely in a future release without any
 * deprecation cycle. Use them only when you are willing to update your code as
 * the library evolves.
 * </p>
 *
 * <p>
 * <strong>Stability contract:</strong> <br>
 * None. Relying on preview elements in production code is discouraged; they are
 * published primarily to gather early feedback.
 * </p>
 */
@Retention(RetentionPolicy.CLASS)
@Target({
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE
})
@Documented
public @interface Preview {
}
