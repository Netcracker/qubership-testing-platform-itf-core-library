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
            String errorMessage = "Incorrect decodeUrl directive format. Please check directive parameters: "
                    + "(Parameter#1 - content, Parameter#2 - encoding name, i.e. \"UTF-8\", \"SHIFT-JIS\", "
                    + "as listed in IANA Charset Registry "
                    + "https://www.iana.org/assignments/character-sets/character-sets.xhtml)";
            rsvc.getLog().error(errorMessage);
            writer.append(errorMessage);
            return true;
        }
        String content = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
        String encoding = String.valueOf(node.jjtGetChild(1).value(internalContextAdapter));
        writer.append(decodeUrl(content, encoding));
        return true;
    }

    /**
     * Decode String from URL-encoded String using specified Encoding.
     *
     * @param content URL-encoded String to be decoded
     * @param encoding String Encoding name according IANA Charset Registry
     * @return String decoded result.
     */
    public String decodeUrl(String content, String encoding) {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        try {
            if (!StringUtils.containsIgnoreCase(encoding.trim(), JIS)) {
                return URLDecoder.decode(content, encoding);
            }
            // below decoding using ISO_8859_1 is needed when we're decoding to Japanese charset
            String decoded = URLDecoder.decode(content, StandardCharsets.ISO_8859_1.name());
            byte[] decodedBytes = decoded.getBytes(StandardCharsets.ISO_8859_1);
            return new String(decodedBytes, encoding);
        } catch (UnsupportedEncodingException ex) {
            String errorMessage = "Unsupported encoding [" + encoding + "] for decodeUrl directive";
            /*
                In case this method is invoked outside Velocity Runtime Services,
                exception won't be logged.
             */
            if (rsvc != null) {
                rsvc.getLog().error(errorMessage);
            }
            return errorMessage;
        }
    }
}
