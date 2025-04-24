/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

    /**
     * How the property would be stored in
     * {@link org.qubership.automation.itf.core.model.transport.ConnectionProperties}.
     */
    String shortName();

    /**
     * How the property would be displayed in UI.
     */
    String longName();

    /**
     * Property placeholder in UI.
     */
    String description();

    /**
     * Is the value mandatory and must be filled?.
     */
    boolean optional() default false;

    /**
     * Property can be configured only from environment.
     */
    boolean fromServer() default false;

    /**
     * Is the property can be configured on template?.
     */
    boolean forTemplate() default false;

    /**
     * Is the property can be configured on server?.
     */
    boolean forServer() default true;

    /**
     * Is the property can be configured on trigger?.
     */
    boolean forTrigger() default true;

    /**
     * It's mean that property would be processed by TemplateEngine.
     */
    boolean isDynamic() default false;

    /**
     * TODO: Add JavaDoc.
     */
    boolean isRedefined() default false;

    /**
     * Loading template by ID.
     */
    boolean loadTemplate() default false;

    /**
     * Create modifications from user configuration like envs.
     */
    boolean userSettings() default false;

    /**
     * TODO: Add JavaDoc.
     */
    int order() default 1;

    /**
     * TODO: Add JavaDoc.
     */
    String fileDirectoryType() default "";

    /**
     * The category of the parameter to be displayed on the UI.
     */
    String uiCategory() default "";

    /**
     * Expression that is used to validate the property on the UI.
     */
    String validatePattern() default "";
}
