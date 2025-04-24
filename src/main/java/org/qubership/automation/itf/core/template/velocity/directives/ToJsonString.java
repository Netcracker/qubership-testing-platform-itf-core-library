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

package org.qubership.automation.itf.core.template.velocity.directives;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ToJsonString extends Directive {
    @Override
    public String getName() {
        return "toJson";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException {
        boolean prettyPrint = false;
        int count = node.jjtGetNumChildren();
        if (count < 1) {
            throw new IllegalArgumentException("Directive '#" + getName() + "' arguments are missed.\n"
                    + "The 1st argument is $obj (to be printed as Json),\n"
                    + "The 2nd (optional) argument is true/false; if true, json is pretty-printed.");
        } else if (count >= 2) {
            Object prettyPrintParameter = node.jjtGetChild(1).value(internalContextAdapter);
            if (prettyPrintParameter instanceof Boolean) {
                prettyPrint = (Boolean)prettyPrintParameter;
            } else {
                prettyPrint = Boolean.parseBoolean(prettyPrintParameter.toString());
            }
        }
        Object obj = node.jjtGetChild(0).value(internalContextAdapter);
        if (obj == null) {
            return true;
        }
        if (obj instanceof JsonContext) {
            writer.append(((JsonContext)obj).getJsonString());
        } else if (obj instanceof Map || obj instanceof List) {
            ObjectMapper mapper = new ObjectMapper();
            if (prettyPrint) {
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            }
            writer.append(mapper.writeValueAsString(obj));
        } else {
            writer.append(obj.toString());
        }
        return true;
    }
}
