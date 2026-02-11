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

package org.qubership.automation.itf.core.util.transport.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants.SESSION_HANDLER_PROCESS_TIMEOUT;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public enum SessionHandler {

    INSTANCE;
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);
    public final int timeout = setTimeout(Config.getConfig().getInt(SESSION_HANDLER_PROCESS_TIMEOUT), 20000);
    private Cache<String, Message> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(timeout, MILLISECONDS).removalListener(new RemovalListener<String, Message>() {
                @Override
                public void onRemoval(@Nonnull RemovalNotification<String, Message> notification) {
                    if (notification.wasEvicted() && notification.getCause().equals(RemovalCause.EXPIRED)) {
                        LockProvider.INSTANCE.notify(notification.getKey());
                        LOGGER.warn("Response message was expired (timeout is {} sec.) "
                                        + "and removed from cache for sessionId {}.",
                                MILLISECONDS.toSeconds(timeout), notification.getKey());
                    }
                }
            }).build();

    /**
     * Nullable, because in some case, the transport can reply with empty message.
     *
     * @param session - transport session id. It must be generated in each receive message
     * @return returns the {@link Message} by session id
     */
    @Nullable
    public Message getMessage(@Nonnull String session) {
        Message message = cache.getIfPresent(session);
        if (message != null) {
            cache.invalidate(session);
        }
        return message;
    }

    /**
     * Don't put null message, when you need just response with empty message
     * Because when the transport will call {@link #getMessage(String)} it will get the null anyway.
     *
     * @param sessionId - the transport session id.
     * @param message   - the message which build in processing of session
     */
    public void addMessage(@Nonnull String sessionId, @Nonnull Message message) {
        cache.put(sessionId, message);
    }

    /** remove method.
     * @param sessionId - session id generated in the ITFAbstractRouteBuilder
     */
    public void remove(@Nonnull String sessionId) {
        if (cache.getIfPresent(sessionId) != null) {
            cache.invalidate(sessionId);
        }
    }

    /**
     * Just to initialize instance not while the 1st inbound message processing,
     * but near the activation of the 1st trigger.
     *
     * @return true.
     */
    public boolean init() {
        return true;
    }

    public void cleanupCache() {
        cache.cleanUp();
    }

    /**
     * Get configured value of the setting (in milliseconds), check it against 0.
     *
     * @param timeout - configured and parsed value.
     * @param defaultValue - default timeout value.
     * @return - checked value.
     */
    private int setTimeout(int timeout, int defaultValue) {
        return timeout > 0 ? timeout : defaultValue;
    }
}
