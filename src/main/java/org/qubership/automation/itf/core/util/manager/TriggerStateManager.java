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

package org.qubership.automation.itf.core.util.manager;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.TriggerConfiguration;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.constants.TransportState;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.transport.manager.TransportRegistryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TriggerStateManager {

    private static final TriggerStateManager INSTANCE = new TriggerStateManager();

    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    private final LoadingCache<String, TransportState> transportStateCache =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String,
                    TransportState>() {
                @Override
                public TransportState load(@Nonnull String transportType) throws Exception {
                    return TransportRegistryManager.getInstance().getState(transportType);
                }
            });

    private TriggerStateManager() {
    }

    public static TriggerStateManager getInstance() {
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public TriggerState getInboundTriggersState(Environment environment) {
        Set<TriggerState> triggerStates = Sets.newHashSetWithExpectedSize(5);
        for (Map.Entry<System, Server> entry : environment.getInbound().entrySet()) {
            if (entry.getValue() != null) {
                for (InboundTransportConfiguration configuration : entry.getValue().getInbounds(entry.getKey())) {
                    triggerStates.add(getInboundTransportConfigurationStatus(configuration));
                }
            } else {
                LOGGER.warn("Server is empty for system {} at environment {}", entry.getKey(), this);
            }
        }
        TriggerState result = stateCalculationForStub(triggerStates);
        return updateStatusForEnvironment(result, environment);
    }

    private TriggerState updateStatusForEnvironment(TriggerState result, Environment environment) {
        if (result == null) {
            result = TriggerState.EMPTY;
            environment.setEnvironmentState(result);
            environment.store();
        } else {
            if (!result.equals(environment.getEnvironmentState())) {
                environment.setEnvironmentState(result);
                environment.store();
            }
        }
        return result;
    }

    private TriggerState getInboundTransportConfigurationStatus(InboundTransportConfiguration configuration) {
        TriggerState result = TriggerState.INACTIVE;
        if (TransportState.READY.equals(getFromCache(configuration))) {
            result = getStabInboundTransportConfigurationStatus(configuration);
        }
        return result;
    }

    // TODO temporary solution. Need to fix this method
    private TriggerState getTriggerStatus(TriggerConfiguration triggerConfiguration) {
        return TriggerState.INACTIVE;
    }

    private TriggerState getStabInboundTransportConfigurationStatus(InboundTransportConfiguration configuration) {
        Set<TriggerState> triggerStates = Sets.newHashSetWithExpectedSize(4);
        for (TriggerConfiguration triggerConfiguration : configuration.getTriggerConfigurations()) {
            triggerStates.add(getTriggerStatus(triggerConfiguration));
        }
        return stateCalculationForStub(triggerStates);
    }

    /**
     * get stab status.
     * Active - all triggers activate and one or many empty
     * Active + Errors - have one active and one error
     * Active not all - have one active and one inactive and no one error
     * Error - all triggers error and one or many empty
     * Inactive - all triggers inactive
     *
     * @return TriggerState
     */
    private TriggerState stateCalculationForStub(Set<TriggerState> triggerStates) {
        if (triggerStates.isEmpty()) {
            return TriggerState.EMPTY;
        }
        if (triggerStates.size() == 1) {
            return triggerStates.iterator().next();
        }
        Map<TriggerState, State> states = getMapStates();
        for (TriggerState triggerState : triggerStates) {
            prepareStates(states, triggerState);
        }
        return validateStates(states);
    }

    private TriggerState validateStates(Map<TriggerState, State> states) {
        for (Map.Entry<TriggerState, State> state : states.entrySet()) {
            if (state.getValue().areAllState) {
                return state.getKey();
            }
        }
        if (states.get(TriggerState.ACTIVE).haveState && (states.get(TriggerState.ERROR).haveState
                || states.get(TriggerState.ACTIVE_ERROR).haveState)) {
            return TriggerState.ACTIVE_ERROR;
        }
        if (states.get(TriggerState.ACTIVE).haveState && (states.get(TriggerState.INACTIVE).haveState
                || states.get(TriggerState.ACTIVE_PART).haveState)) {
            return TriggerState.ACTIVE_PART;
        }
        if (states.get(TriggerState.ACTIVE_ERROR).haveState) {
            return TriggerState.ACTIVE_ERROR;
        }
        if (states.get(TriggerState.ACTIVE_PART).haveState) {
            return TriggerState.ACTIVE_PART;
        }
        if (states.get(TriggerState.EMPTY).haveState
                && !states.get(TriggerState.ERROR).haveState
                && !states.get(TriggerState.INACTIVE).haveState
                && !states.get(TriggerState.ACTIVE).haveState
                && !states.get(TriggerState.ACTIVE_ERROR).haveState
                && !states.get(TriggerState.ACTIVE_PART).haveState) {
            return TriggerState.EMPTY;
        }
        if (states.get(TriggerState.ACTIVE).haveState && states.get(TriggerState.EMPTY).haveState) {
            return TriggerState.ACTIVE;
        }
        if (states.get(TriggerState.EMPTY).haveState && states.get(TriggerState.ERROR).haveState) {
            return TriggerState.ERROR;
        }
        return TriggerState.INACTIVE;
    }

    private void prepareStates(Map<TriggerState, State> states, TriggerState triggerState) {
        if (Objects.isNull(triggerState)) {
            triggerState = TriggerState.EMPTY;
        }
        for (Map.Entry<TriggerState, State> state : states.entrySet()) {
            prepareState(state, triggerState);
        }
    }

    private void prepareState(Map.Entry<TriggerState, State> state, TriggerState triggerState) {
        if (!triggerState.equals(state.getKey())) {
            state.getValue().areAllState = false;
        } else {
            state.getValue().haveState = true;
        }
    }

    private Map<TriggerState, State> getMapStates() {
        Map<TriggerState, State> states = Maps.newHashMapWithExpectedSize(3);
        states.put(TriggerState.ACTIVE, new State(true, false));
        states.put(TriggerState.ERROR, new State(true, false));
        states.put(TriggerState.INACTIVE, new State(true, false));
        states.put(TriggerState.ACTIVE_PART, new State(true, false));
        states.put(TriggerState.ACTIVE_ERROR, new State(true, false));
        states.put(TriggerState.EMPTY, new State(true, false));
        return states;
    }

    private TransportState getFromCache(InboundTransportConfiguration configuration) {
        try {
            return transportStateCache.get(configuration.getReferencedConfiguration().getTypeName());
        } catch (ExecutionException e) {
            LOGGER.warn("Unable to get transport state from cache", e);
            return TransportRegistryManager.getInstance().getState(configuration
                    .getReferencedConfiguration().getTypeName());
        }
    }

    private static class State {

        private Boolean areAllState = true;
        private Boolean haveState = false;

        private State(Boolean areAllState, Boolean haveState) {
            this.areAllState = areAllState;
            this.haveState = haveState;
        }
    }
}
