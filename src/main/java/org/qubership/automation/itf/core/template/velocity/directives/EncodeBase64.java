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
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class EncodeBase64 extends Directive {
    @Override
    public String getName() {
        return "encode_base64";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (node.jjtGetChild(i) != null) {
                String stringValue = String.valueOf(node.jjtGetChild(i).value(internalContextAdapter));
                writer.append(encodeContent(stringValue));
            }
        }
        return true;
    }

    /**
     * Encode content String into base64-encoded String using UTF_8 charset.
     *
     * @param content String to be encoded
     * @return encoded String.
     */
    public String encodeContent(String content) {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        return new String(Base64.encodeBase64(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
