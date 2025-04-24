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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class JsonPathDirective extends Directive {

    @Override
    public String getName() {
        return "json_path";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node)
            throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException {
        int count = node.jjtGetNumChildren();
        if (count < 2) {
            throw new IllegalArgumentException("Directive '#" + getName()
                    + "' arguments is missing. 1st argument is $json, second+ is JsonPath's.");
        }
        String json = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
        DocumentContext documentContext = JsonPath.parse(json);
        for (int index = 1; index < count; index++) {
            String result = documentContext.read(
                    node.jjtGetChild(index).value(internalContextAdapter).toString()
            ).toString();
            writer.append(result);
        }
        return true;
    }
}
