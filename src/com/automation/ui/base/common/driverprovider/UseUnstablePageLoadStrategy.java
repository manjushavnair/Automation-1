package com.automation.ui.base.common.driverprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate test method if you want to use
 * unstable page load strategy for firefox. This causes firefox not to wait for full page load
 * before executing WebDriver commands.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface UseUnstablePageLoadStrategy {

}
