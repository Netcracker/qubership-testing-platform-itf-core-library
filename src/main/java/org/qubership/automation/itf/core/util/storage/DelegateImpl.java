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

package org.qubership.automation.itf.core.util.storage;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import com.google.common.collect.Maps;

public class DelegateImpl implements StoreInformationDelegate<Object, Integer>, Serializable {
    private static final long serialVersionUID = 20240812L;

    private Object ID;
    private Integer version;
    private Map<Class, Object> additionalInformation;

    public DelegateImpl() {
    }

    @Override
    public Object getID() {
        return ID;
    }

    @Override
    public void setID(Object id) {
        if (id instanceof String) {
            try {
                this.ID = new BigInteger((String) id);
            } catch (NumberFormatException e) {
                this.ID = id;
            }
        } else if (id instanceof Long) {
            this.ID = BigInteger.valueOf((long) id);
        } else {
            this.ID = id;
        }
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public <T> T get(Class<T> clazz) {
        if (additionalInformation == null) {
            return null;
        }
        return clazz.cast(additionalInformation.get(clazz));
    }

    @Override
    public <T> void put(Class<T> clazz, T info) {
        if (additionalInformation == null) {
            additionalInformation = Maps.newHashMapWithExpectedSize(2);
        }
        additionalInformation.put(clazz, info);
    }

}
