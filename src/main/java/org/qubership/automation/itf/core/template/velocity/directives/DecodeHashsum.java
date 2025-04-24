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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;

public class DecodeHashsum extends Directive {

    @Override
    public String getName() {
        return "decode_hashsum";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        /* Positional parameters:
            1st parameter: Secure Hash Algorithm name, in the form:
                RS256;RS512,
            2nd parameter: content to compute hashsum,
         */
        String algorithm;
        String content;
        int paramsCount = node.jjtGetNumChildren();
        if (paramsCount == 2) {
            algorithm = getString(node.jjtGetChild(0), internalContextAdapter);
            content = getString(node.jjtGetChild(1), internalContextAdapter);
        } else {
            throw new IllegalArgumentException("#" + getName() + ": Incorrect number of parameters! "
                    + getExceptionMessage());
        }
        if (algorithm != null && content != null) {
            writer.append(decode_computeHash(algorithm.toUpperCase(), content));
        } else {
            throw new IllegalArgumentException("#" + getName() + ": Parameters must be non-null! "
                    + getExceptionMessage());
        }
        return true;
    }

    private String getString(Node node, InternalContextAdapter internalContextAdapter) {
        return (node == null) ? null : String.valueOf(node.value(internalContextAdapter));
    }

    private String decode_computeHash(String algorithm, String content) throws IllegalArgumentException {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        switch (algorithm) {
            case "RS256":
            case "RS512": {
                return decodeEncryptJws(content, algorithm);
            }
            default: {
                throw new IllegalArgumentException("Unknown algorithm '" + algorithm + "'. " + getExceptionMessage());
            }
        }
    }

    private static String decodeEncryptJws(String toBeEncrypt, String algorithmName) {
        try {
            JWSObject jwsObject = JWSObject.parse(toBeEncrypt);
            Payload payload = jwsObject.getPayload();
            return payload.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Exception while computing " + algorithmName + " algorithm: "
                    + getExceptionMessage(), e);
        }
    }

    private static String getExceptionMessage() {
        return "Directive '#decode_hashsum': 1st argument is $algorithm "
                + "('RS256', 'RS512'), "
                + "2nd argument is $content.";
    }
}
