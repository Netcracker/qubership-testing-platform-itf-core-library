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

package org.qubership.automation.itf.core.util.engine;

import javax.inject.Inject;
import javax.inject.Named;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Factory with an ability to pass an instance thru constructor using DI.
 * If instance is not set before 'get' method invoked, the DEFAULT strategy is used
 * You can {@link #init(Supplier)} singleton manually
 */
public class TemplateEngineFactory {

    private static final Supplier<TemplateEngine> DEFAULT = new Supplier<TemplateEngine>() {
        @Override
        public TemplateEngine get() {
            throw new RuntimeException("Please set TemplateEngine as \"templateEngine\" for TemplateEngineFactory");
        }
    };

    private static volatile TemplateEngine INSTANCE;

    @Inject
    protected TemplateEngineFactory(@Named("templateEngine") TemplateEngine factory) {
        init(factory);
    }

    public static TemplateEngine get() {
        init(DEFAULT);
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void init(Supplier<TemplateEngine> instance) {
        if (INSTANCE == null) {
            synchronized (TemplateEngineFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = instance.get();
                }
            }
        }
    }

    public static void init(TemplateEngine instance) {
        init(Suppliers.ofInstance(instance));
    }

    public static String process(Storable owner, String someString, JsonContext context) {
        return get().process(owner, someString, context);
    }

    public static String process(Storable owner, String someString, JsonContext context, String coords) {
        return get().process(owner, someString, context, coords);
    }
}
