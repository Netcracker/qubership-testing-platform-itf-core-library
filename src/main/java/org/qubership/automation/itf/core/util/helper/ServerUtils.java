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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.converter.PropertiesConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.transport.access.AccessOutboundTransport;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.manager.TransportRegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Striped;

public class ServerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerUtils.class);
    private static final Striped<ReadWriteLock> SERVER_LOCKS = Striped.lazyWeakReadWriteLock(256);

    /**
     * Calculate ConnectionProperties by combining them from different sources.
     *
     * @return ConnectionProperties processed
     */
    public static ConnectionProperties calculate(@Nonnull Server server,
                                                 @Nonnull System receiver,
                                                 @Nonnull TransportConfiguration configuration,
                                                 @Nonnull Message message,
                                                 Template template) throws TransportException {
        String typeName = configuration.getTypeName();
        OutboundTransportConfiguration out = server.getOutbound(receiver, typeName);
        return PropertiesConverter.convert(typeName,
                configuration,
                out,
                message.getTransportProperties(), template.getTransportProperties(typeName));
    }

    /**
     * Calculate ConnectionProperties by combining them from different sources.
     * Parse values via Velocity using instanceContext parameter.
     *
     * @return ConnectionProperties processed
     */
    public static ConnectionProperties calculate(@Nonnull Server server,
                                                 @Nonnull System receiver,
                                                 @Nonnull TransportConfiguration configuration,
                                                 @Nonnull Message message,
                                                 Template template,
                                                 InstanceContext instanceContext) throws TransportException {
        String typeName = configuration.getTypeName();
        OutboundTransportConfiguration out = server.getOutbound(receiver, typeName);
        return PropertiesConverter.convert(instanceContext, typeName,
                configuration,
                out,
                message.getTransportProperties(), template.getTransportProperties(typeName));
    }

    /**
     * opens new transaction for write operations if necessary.
     *
     * @return updated by JPA instance of server in case if write has been performed
     */
    public static Server syncOutbounds(@Nonnull Server server,
                                       @Nonnull System system) {
        return syncOutbounds(server, Collections.singleton(system));
    }

    /**
     * opens new transaction for write operations if necessary.
     *
     * @return updated by JPA instance of server in case if write has been performed
     */
    public static Server syncOutbounds(@Nonnull final Server server,
                                       @Nonnull final Iterable<? extends System> systems) {

        Map<String, AccessTransport> transports;
        try {
            transports = TransportRegistryManager.getInstance().getTransports();
        } catch (TransportException e) {
            LOGGER.error("Can not sync outbounds", e);
            return server;
        }

        Server actualServer = server;
        final Object serverId = server.getID();
        final ObjectManager<Server> serverObjectManager = CoreObjectManager.getInstance().getManager(Server.class);
        for (Map.Entry<String, AccessTransport> entry : transports.entrySet()) {
            if (!(entry.getValue() instanceof AccessOutboundTransport)) {
                continue;
            }
            //for each outbound transport type
            final String typeName = entry.getKey();

            ReadWriteLock lock = SERVER_LOCKS.get(server);
            final Server actual = actualServer;
            final ImmutableCollection<? extends System> unregistered = doRead(
                    new Callable<ImmutableCollection<? extends System>>() {
                        @Override
                        public ImmutableCollection<? extends System> call() throws Exception {
                            return filterRegistered(typeName, systems, actual);
                        }
                    }, lock);

            if (unregistered.isEmpty()) {
                continue;
            }

            actualServer = doWrite(new Callable<Server>() {
                @Override
                public Server call() throws Exception {
                    return fillUnregistered(serverId, serverObjectManager, typeName, unregistered);
                }
            }, lock);
        }
        return actualServer;
    }

    /**
     * opens new transaction for write operations if necessary.
     *
     * @return updated by JPA instance of server in case if write has been performed
     */
    public static Server syncInbounds(@Nonnull Server server,
                                      @Nonnull System system) {
        return syncInbounds(server, Collections.singleton(system));
    }

    /**
     * opens new transaction for write operations if necessary.
     *
     * @return updated by JPA instance of server in case if write has been performed
     */
    public static Server syncInbounds(@Nonnull final Server server,
                                      @Nonnull final Iterable<? extends System> systems) {
        ReadWriteLock lock = SERVER_LOCKS.get(server);

        final ImmutableCollection<TransportConfiguration> unregistered = doRead(
                new Callable<ImmutableCollection<TransportConfiguration>>() {
                    @Override
                    public ImmutableCollection<TransportConfiguration> call() throws Exception {
                        return filterRegistered(server, systems);
                    }
                }, lock);

        if (unregistered.isEmpty()) {
            return server;
        }

        final ObjectManager<Server> serverObjectManager = CoreObjectManager.getInstance().getManager(Server.class);
        return doWrite(new Callable<Server>() {
            @Override
            public Server call() throws Exception {
                return fillUnregistered(server.getID(), serverObjectManager, unregistered);
            }
        }, lock);
    }

    //region inbounds utils

    private static Server fillUnregistered(Object serverId,
                                           ObjectManager<Server> serverObjectManager,
                                           Collection<TransportConfiguration> unregistered) {
        //ensure that inbounds has not been updated yet (by another thread for ex)
        Server server = serverObjectManager.getById(serverId);
        unregistered = filterRegistered(server, unregistered);
        if (!unregistered.isEmpty()) {
            //update inbounds
            Collection<InboundTransportConfiguration> inbounds = server.getInbounds();
            for (TransportConfiguration config : unregistered) {
                inbounds.add(new InboundTransportConfiguration(config, server));
            }
            server.store();
        }
        return server;
    }

    private static Server fillUnregistered(Object serverId,
                                           ObjectManager<Server> serverObjectManager,
                                           final String typeName,
                                           ImmutableCollection<? extends System> unregistered) {
        //ensure that outbounds has not been updated yet (by another thread for ex)
        Server server = serverObjectManager.getById(serverId);
        unregistered = filterRegistered(typeName, unregistered, server);
        if (!unregistered.isEmpty()) {
            //update outbounds
            Collection<OutboundTransportConfiguration> outbounds = server.getOutbounds();
            for (System system : unregistered) {
                outbounds.add(new OutboundTransportConfiguration(typeName, server, system));
            }
            server.store();
        }
        return server;
    }

    private static ImmutableCollection<TransportConfiguration> filterRegistered(final Server where,
                                                                                Collection<TransportConfiguration>
                                                                                        configs) {
        return FluentIterable.from(configs).filter(new Predicate<TransportConfiguration>() {
            @Override
            public boolean apply(TransportConfiguration input) {
                if (input == null) {
                    return false;
                }
                Mep mep = input.getMep();
                return mep.isInbound() && mep.isRequest() && !containsInbound(where, input);
            }
        }).toList();
    }

    private static ImmutableCollection<? extends System> filterRegistered(final String typeName,
                                                                          Iterable<? extends System> allSystems,
                                                                          final Server server) {
        return FluentIterable.from(allSystems).filter(new Predicate<System>() {
            @Override
            public boolean apply(System input) {
                //will drop all registered
                return !containsOutbound(server.getOutbounds(), input, typeName);
            }
        }).toList();
    }

    private static ImmutableCollection<TransportConfiguration> filterRegistered(final Server where,
                                                                                Iterable<? extends System> systems) {
        Iterable<TransportConfiguration> configs = Iterables.concat(
                FluentIterable.from(systems)
                        .transform(new Function<System, Iterable<TransportConfiguration>>() {
                            @Override
                            public Iterable<TransportConfiguration> apply(System input) {
                                return input == null ? new HashSet<>() : input.getTransports();
                            }
                        }));
        return filterRegistered(where, ImmutableList.copyOf(configs));
    }

    private static boolean containsInbound(Server who, final TransportConfiguration what) {
        return Iterables.tryFind(who.getInbounds(), new Predicate<InboundTransportConfiguration>() {
            @Override
            public boolean apply(InboundTransportConfiguration input) {
                return input != null && what.equals(input.getReferencedConfiguration());
            }
        }).isPresent();
    }

    //endregion

    //region outbounds utils

    private static boolean containsOutbound(Collection<OutboundTransportConfiguration> in,
                                            final System what, final String typeName) {
        return Iterables.tryFind(in, new Predicate<OutboundTransportConfiguration>() {
            @Override
            public boolean apply(OutboundTransportConfiguration input) {
                return input != null && what.equals(input.getSystem()) && typeName.equals(input.getTypeName());
            }
        }).isPresent();
    }

    //endregion

    private static <T> T doRead(Callable<T> callable, ReadWriteLock lock) {
        Lock readLock = lock.readLock();
        readLock.lock();
        try {
            return TxExecutor.executeUnchecked(callable, TxExecutor.readOnlyTransaction());
        } finally {
            readLock.unlock();
        }
    }

    private static <T> T doWrite(Callable<T> callable, ReadWriteLock lock) {
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            return TxExecutor.executeUnchecked(callable, TxExecutor.nestedWritableTransaction());
        } finally {
            writeLock.unlock();
        }
    }
}

