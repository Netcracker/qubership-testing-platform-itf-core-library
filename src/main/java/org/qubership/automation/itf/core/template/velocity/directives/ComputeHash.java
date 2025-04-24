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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;

public class ComputeHash extends Directive {
    @Override
    public String getName() {
        return "hashsum";
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
                SHA-1,
            2nd parameter: content to compute hashsum,
            3rd parameter: encoding (if this parameter is absent, the UTF-8 encoding will be used by default.)
            4th parameter: key - currently only for HMAC-SHA256

            Extra processing:
                1) We ignore case in the 1st parameter,
                2) Default algorithm is SHA-1, so if there is only one parameter, it is considered as content,
                and algorithm SHA-1 is used
         */
        String algorithm;
        String content;
        String encoding = "UTF-8";
        String key = null;
        Boolean encodeToBase64 = null;
        int paramsCount = node.jjtGetNumChildren();
        switch (paramsCount) {
            case 1:
                algorithm = "SHA-1";
                content = getString(node.jjtGetChild(0), internalContextAdapter);
                break;
            case 2:
                algorithm = getString(node.jjtGetChild(0), internalContextAdapter);
                content = getString(node.jjtGetChild(1), internalContextAdapter);
                break;
            case 3:
                algorithm = getString(node.jjtGetChild(0), internalContextAdapter);
                content = getString(node.jjtGetChild(1), internalContextAdapter);
                encoding = getString(node.jjtGetChild(2), internalContextAdapter);
                break;
            case 4:
                algorithm = getString(node.jjtGetChild(0), internalContextAdapter);
                content = getString(node.jjtGetChild(1), internalContextAdapter);
                encoding = getString(node.jjtGetChild(2), internalContextAdapter);
                key = getString(node.jjtGetChild(3), internalContextAdapter);
                encodeToBase64 = false; // need only for 'hmacXxxToHex'
                break;
            case 5:
                algorithm = getString(node.jjtGetChild(0), internalContextAdapter);
                content = getString(node.jjtGetChild(1), internalContextAdapter);
                encoding = getString(node.jjtGetChild(2), internalContextAdapter);
                key = getString(node.jjtGetChild(3), internalContextAdapter);
                encodeToBase64 = Boolean.parseBoolean(getString(node.jjtGetChild(4), internalContextAdapter));
                break;
            default:
                // It's discussable: to log warn or to throw an exception
                rsvc.getLog().warn("Incorrect format. " + getExceptionMessage());
                return true;
        }

        if (algorithm != null && content != null) {
            writer.append(computeHash(algorithm.toUpperCase(), content, encoding, key, encodeToBase64));
        }
        return true;
    }

    private String getString(Node node, InternalContextAdapter internalContextAdapter) {
        return node != null ? String.valueOf(node.value(internalContextAdapter)) : null;
    }

    private String computeHash(String algorithm, String content, String encoding, String key, Boolean encodeToBase64)
            throws UnsupportedEncodingException, IllegalArgumentException {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }
        switch (algorithm) {
            case "SHA-1":
                return DigestUtils.sha1Hex(content.getBytes(encoding));
            case "SHA-256":
                return DigestUtils.sha256Hex(content.getBytes(encoding));
            case "SHA-384":
                return DigestUtils.sha384Hex(content.getBytes(encoding));
            case "SHA-512":
                return DigestUtils.sha512Hex(content.getBytes(encoding));
            case "MD2":
                return DigestUtils.md2Hex(content.getBytes(encoding));
            case "MD5":
                return DigestUtils.md5Hex(content.getBytes(encoding));
            case "AES-256":
                try {
                    return encryptAes(content.getBytes(encoding), getIvKeyZeroBased(encoding), key.getBytes(encoding));
                } catch (NoSuchPaddingException | BadPaddingException  | NoSuchAlgorithmException
                        | IllegalBlockSizeException e) {
                    throw new IllegalArgumentException("Error occurred during encryption '" + algorithm + "'. \n"
                            + getExceptionMessage());
                } catch (InvalidKeyException e) {
                    throw new IllegalArgumentException("Error occurred during encryption '" + algorithm
                            + "'. Key should contain 32 bytes. \n" + getExceptionMessage());
                } catch (InvalidAlgorithmParameterException e) {
                    throw new IllegalArgumentException("Error occurred during encryption '" + algorithm
                            + "'. Only 8-bit encoding allowed for this algorithm: UTF-8, Windows 1251, etc. \n"
                            + getExceptionMessage());
                }
            case "CRC-32":
                return crc32Hex(content.getBytes(encoding));
            case "RS256":
                return encryptJws(content.getBytes(encoding), key.getBytes(encoding), JWSAlgorithm.RS256, algorithm);
            case "RS512":
                return encryptJws(content.getBytes(encoding), key.getBytes(encoding), JWSAlgorithm.RS512, algorithm);
            default:
                if (StringUtils.startsWithIgnoreCase(algorithm, "HMAC")) {
                    return hmacXxxToHex(algorithm, content.getBytes(encoding), key.getBytes(encoding), encodeToBase64);
                } else {
                    // It's discussable: to log warn or to throw an exception
                    throw new IllegalArgumentException("Wrong algorithm '" + algorithm + "'. " + getExceptionMessage());
                }
        }
    }

    private static byte[] getIvKeyZeroBased(String encoding) throws UnsupportedEncodingException {
        final String ivKeyZeroBased = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
        return ivKeyZeroBased.getBytes(encoding);
    }

    private static String crc32Hex(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return Long.toHexString(crc.getValue());
    }

    private static String hmacXxxToHex(String algorithm, byte[] data, byte[] key, Boolean encodeToBase64)
            throws IllegalArgumentException {
        try {
            String algorithmName = "Hmac" + algorithm.substring(4).replace("-", StringUtils.EMPTY).toUpperCase();
            Mac xxxHmac = Mac.getInstance(algorithmName);
            SecretKeySpec secretKey = new SecretKeySpec(key, algorithmName);
            xxxHmac.init(secretKey);
            byte[] digest = xxxHmac.doFinal(data);
            return encodeToBase64 ? Base64.getEncoder().encodeToString(digest) : Hex.encodeHexString(digest);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalArgumentException("Exception while computing '" + algorithm + "'. "
                    + getExceptionMessage(), ex);
        }
    }

    private static String encryptAes(byte[] toBeEncrypt, byte[] ivkey, byte[] key) throws NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivkey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(toBeEncrypt));
    }

    private static String encryptJws(byte[] toBeEncrypt, byte[] key, JWSAlgorithm jwsAlgorithm, String algorithmName) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
            JWSSigner signer = new RSASSASigner(privateKey, true);
            Payload contentPayload = new Payload(toBeEncrypt);
            JWSObject jwsObject = new JWSObject(new JWSHeader(jwsAlgorithm), contentPayload);
            jwsObject.sign(signer);
            return jwsObject.serialize();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException e) {
            throw new IllegalArgumentException("Exception while computing " + algorithmName + " algorithm: "
                    + getExceptionMessage(), e);
        }
    }

    private static String getExceptionMessage() {
        return "Directive '#hashsum': 1st argument is $algorithm \n('SHA-1', 'SHA-256', "
                + "'SHA-384', 'SHA-512', 'MD2', 'MD5', 'CRC-32', 'HMAC-xxx', 'AES-256', 'RS256', 'RS512'), \n"
                + "2nd argument is $content, \n"
                + "3rd argument is $encoding (this argument can be empty - UTF-8 by default), \n"
                + "4th argument is $key (key is needed for 'HMAC-xxx', 'AES-256', 'RS256' and 'RS512'),\n"
                + "5th argument = \"true\" (encode digest via Base64) or \"false\" (encode digest via "
                + "Hex.encodeHexString) (default is false) (only for 'HMAC-xxx' currently),\n"
                + " where 'HMAC-xxx' are: HMAC-SHA256, HMAC-SHA1, HMAC-SHA384 and so on.";
    }
}
