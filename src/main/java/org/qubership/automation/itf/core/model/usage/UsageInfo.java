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

package org.qubership.automation.itf.core.model.usage;

import java.util.List;

import org.qubership.automation.itf.core.model.common.Storable;

import com.google.common.collect.Lists;

public class UsageInfo {

    public UsageInfo() {
    }

    private Storable referer;
    private Storable[] path;
    private String property;

    public Storable getReferer() {
        return referer;
    }

    public void setReferer(Storable referer) {
        this.referer = referer;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Storable[] getPath() {
        if (path == null) {
            List<Storable> path = Lists.newArrayListWithExpectedSize(5);
            Storable parent = referer.getParent();
            while (parent != null) {
                path.add(parent);
                parent = referer.getParent();
            }
            this.path = new Storable[path.size()];
            this.path = Lists.reverse(path).toArray(this.path);
        }
        return path;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
