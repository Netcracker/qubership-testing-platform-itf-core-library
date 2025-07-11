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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class DecodeUrl extends Directive {
    private static final String JIS = "jis";

    @Override
    public String getName() {
        return "decodeUrl";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        if (node.jjtGetNumChildren() != 2) {
            rsvc.getLog().error("Incorrect #decodeUrl directive format. Check directive parameters: "
                    + "(Parameter#1 - content, Parameter#2 - encoding name, i.e. \"UTF-8\", \"SHIFT-JIS\")");
            writer.append("#err");
            return true;
        }
        String content = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
        String encoding = String.valueOf(node.jjtGetChild(1).value(internalContextAdapter));
        writer.append(decodeUrl(content, encoding));
        return true;
    }


    private String decodeUrl(String content, String enc) {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        try {
            if (!StringUtils.containsIgnoreCase(enc.trim(), JIS)) {
                return URLDecoder.decode(content, enc);
            }
            // below decoding using ISO_8859_1 is needed when we decoding to Japanese charset
            String decoded = URLDecoder.decode(content, StandardCharsets.ISO_8859_1.name());
            byte[] decodedBytes = decoded.getBytes(StandardCharsets.ISO_8859_1.name());
            return new String(decodedBytes, enc);
        } catch (UnsupportedEncodingException e) {
            rsvc.getLog().error("Unsupported encoding [" + enc + "] for #decodeUrl directive");
        }
        return "#err";
    }
}
