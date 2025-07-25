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

package org.qubership.automation.itf.core.model.key;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.engine.TemplateEngineFactory;
import org.qubership.automation.itf.core.util.exception.KeyDefinitionException;

public class ByTemplateKeyDefinition extends AbstractStorable implements KeyDefinition {
    private static final long serialVersionUID = 20240812L;

    private String macro;

    public ByTemplateKeyDefinition() {
    }

    public ByTemplateKeyDefinition(String macro) {
        this.macro = macro;
    }

    @Override
    public String defineKey(InstanceContext context) throws KeyDefinitionException {
        Storable parent = getParent();
        if (parent != null) {
            return TemplateEngineFactory.process(parent, macro, context).trim();
        } else {
            return TemplateEngineFactory.process(null, macro, context).trim();
        }
    }

    public String getExpression() {
        return macro;
    }

    public void setExpression(String macro) {
        this.macro = macro;
    }
}
