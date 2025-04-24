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

package org.qubership.automation.itf.core.util.helper;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.content.JsonContentProvider;
import org.qubership.automation.itf.core.util.provider.content.MessageContentProvider;
import org.qubership.automation.itf.core.util.provider.content.XmlContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class ContentHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentHelper.class);

    private static ContentHelper instance = new ContentHelper();
    private final Map<String, MessageContentProvider<?>> providers = Maps.newHashMapWithExpectedSize(3);

    public static ContentHelper getInstance() {
        return instance;
    }

    private ContentHelper() {
        initDefault();
    }

    private void initDefault() {
        providers.put(ParsingRuleType.XPATH.toString(), new XmlContentProvider());
        providers.put(ParsingRuleType.JSON_PATH.toString(), new JsonContentProvider());
    }

    public void registerProvider(String parsingRuleType, MessageContentProvider<?> provider) {
        providers.put(parsingRuleType, provider);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void trySetContent(Message message, String parsingRuleType) throws ContentException {
        if (message.getContent() == null) {
            MessageContentProvider<?> provider = providers.get(parsingRuleType);
            if (provider != null && provider.supports(message)) {
                message.setContent(provider.provide(message));
            }
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public boolean tryForContentType(Class<?> requestedContentType, Message message) {
        return message.getContent() != null
                && requestedContentType.isAssignableFrom((Class<?>) ((ParameterizedType) message.getContent()
                .getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]);
    }
}
