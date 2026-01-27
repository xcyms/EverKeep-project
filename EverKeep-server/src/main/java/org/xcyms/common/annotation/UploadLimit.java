package org.xcyms.common.annotation;

import java.lang.annotation.*;

/**
 * 上传限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UploadLimit {
    /**
     * 每小时限制上传数量，默认 50
     */
    int hour() default 50;

    /**
     * 每天限制上传数量，默认 200
     */
    int day() default 200;

    /**
     * 每月限制上传数量，默认 2000
     */
    int month() default 2000;
}