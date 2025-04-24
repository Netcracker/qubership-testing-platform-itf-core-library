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

package org.qubership.automation.itf.core.util.holder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.qubership.automation.itf.core.model.jpa.system.stub.Listener;

import com.google.common.collect.Maps;

public class EventTriggerHolder {
    private Map<Object, Listener> listenerMap = Maps.newConcurrentMap();
    private Map<Object, List<Listener>> situationOnStartListenersMap = Maps.newConcurrentMap();
    private static final EventTriggerHolder INSTANCE = new EventTriggerHolder();

    public static EventTriggerHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Add event listener into listeners' Maps based on isStartListener parameter.
     */
    public void add(Listener listener, boolean isStartListener) {
        listenerMap.put(listener.getId(), listener);
        if (isStartListener) {
            synchronized (listener.getSituationId()) {
                List<Listener> list = situationOnStartListenersMap
                        .getOrDefault(listener.getSituationId(), new ArrayList<>());
                list.add(listener);
                situationOnStartListenersMap.put(listener.getSituationId(), list);
            }
        }
    }

    /**
     * Remove event listener from listeners' Maps based on isStartListener parameter.
     */
    public void remove(Listener listener, boolean isStartListener) {
        if (listener != null) {
            listenerMap.remove(listener.getId());
            if (isStartListener) {
                synchronized (listener.getSituationId()) {
                    List<Listener> list = situationOnStartListenersMap.get(listener.getSituationId());
                    if (list != null) {
                        list.remove(listener);
                    }
                }
            }
        }
    }

    public Map<Object, Listener> getAll() {
        return listenerMap;
    }

    public Listener get(Object id) {
        return listenerMap.get(id);
    }

    public List<Listener> getOnStartSituationListeners(Object id) {
        return situationOnStartListenersMap.get(id);
    }
}
