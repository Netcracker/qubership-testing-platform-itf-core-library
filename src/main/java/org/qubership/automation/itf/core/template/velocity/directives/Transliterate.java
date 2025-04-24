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

import com.ibm.icu.text.Transliterator;

public class Transliterate extends Directive {
    @Override
    public String getName() {
        return "transliterate";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 2) {
            rsvc.getLog().error("Incorrect #transliterate directive format: (Parameter#1 - content, "
                    + "Parameter#2 - from-to name, i.e. \"Halfwidth-Fullwidth\", \"Fullwidth-Halfwidth\")");
            return false;
        }
        String content = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
        String fromTo = String.valueOf(node.jjtGetChild(1).value(internalContextAdapter));
        writer.append(transliterate(content , fromTo));
        return true;
    }

    private String transliterate(String content, String fromTo) {
        try {
            Transliterator transliterator = Transliterator.getInstance(fromTo);
            return transliterator.transliterate(content);
        } catch (Exception e) {
            rsvc.getLog().error("Unable to transliterate content. Cause: " + e.getMessage());
        }
        return "#err";
    }

}
