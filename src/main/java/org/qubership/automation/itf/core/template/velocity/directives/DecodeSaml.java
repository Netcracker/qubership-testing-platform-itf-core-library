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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class DecodeSaml extends Directive {
    @Override
    public String getName() {
        return "decode_saml";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        String encoding;
        if (node.jjtGetNumChildren() == 1) {
            encoding = "UTF-8";
        } else if (node.jjtGetNumChildren() == 2) {
            if (node.jjtGetChild(1) != null) {
                encoding = String.valueOf(node.jjtGetChild(1).value(internalContextAdapter));
            } else {
                encoding = "UTF-8";
            }
        } else {
            rsvc.getLog().error("Incorrect #decode_saml directive format. Check directive parameters: "
                    + "Parameter#1 - content, Parameter#2 (optional)- encoding name, i.e. \"UTF-8\", \"SHIFT-JIS\"");
            writer.append("#decode_saml:incorrect parameters");
            return true;
        }
        if (node.jjtGetChild(0) != null) {
            String stringValue = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
            writer.append(decodeSaml(stringValue, encoding));
        }
        return true;
    }

    /**
     * Decode text String to Saml using provided encoding.
     */
    public String decodeSaml(String text, String encoding) {
        try {
            byte[] decodedBytes = Base64.decodeBase64(java.net.URLDecoder.decode(text, encoding));
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(decodedBytes);
            InflaterInputStream in = new InflaterInputStream(bytesIn, new Inflater(true));
            byte[] buffer = new byte[decodedBytes.length];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int bytesRead = 0; bytesRead != -1; bytesRead = in.read(buffer)) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toString(encoding);
        } catch (IOException e) {
            throw new RuntimeException("Exception while #decode_saml directive processing: "
                    + e + (e.getCause() == null ? StringUtils.EMPTY : "\nCaused by: " + e.getCause().toString()));
        }
    }
}
