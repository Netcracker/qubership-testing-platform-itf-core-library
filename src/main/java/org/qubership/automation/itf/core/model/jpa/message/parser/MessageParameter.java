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

package org.qubership.automation.itf.core.model.jpa.message.parser;

import java.util.List;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@Entity
@JsonFilter("reportWorkerFilter_MessageParameter")
public class MessageParameter extends AbstractStorable {
    private static final long serialVersionUID = 20240812L;

    @JsonIgnore
    private Storable parent;

    private String paramName;
    private List<String> multipleValue;
    private boolean multiple;
    private boolean autosave;

    private MessageParameter() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Builder build(String paramName, ParsingRule rule) {
        Builder builder = new Builder();
        builder.paramName = paramName;
        return builder;
    }

    public String getParamName() {
        return paramName;
    }

    public String getSingleValue() {
        return multipleValue.get(0);
    }

    public List<String> getMultipleValue() {
        return multipleValue;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public boolean isAutosave() {
        return autosave;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public void setMultipleValue(List<String> multipleValue) {
        this.multipleValue = multipleValue;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
    }

    @Override
    public String toString() {
        return String.format("%s / %s [id: %s]", this.getParent().getClass().getSimpleName(),
                this.paramName, this.getID());
    }

    public static class Builder {
        private String paramName;
        private List<String> multipleValue = Lists.newArrayListWithExpectedSize(10);
        private boolean multiple = false;
        private boolean autosave = false;

        private Builder() {
        }

        /**
         * TODO: Add JavaDoc.
         */
        public Builder setAutosave(boolean autosave) {
            this.autosave = autosave;
            return this;
        }

        /**
         * TODO: Add JavaDoc.
         */
        public Builder singleValue(String singleValue) {
            multipleValue.clear();
            multipleValue.add(singleValue);
            return this;
        }

        public Builder multipleValue(String value) {
            multipleValue.add(value);
            return this;
        }

        /**
         * TODO: Add JavaDoc.
         */
        public Builder multiple(boolean multiple) {
            this.multiple = multiple;
            return this;
        }

        /**
         * TODO: Add JavaDoc.
         */
        public MessageParameter get() {
            MessageParameter parameter = new MessageParameter();
            parameter.paramName = paramName;
            parameter.multiple = multiple;
            parameter.multipleValue = multipleValue;
            parameter.autosave = autosave;
            return parameter;
        }
    }
}
