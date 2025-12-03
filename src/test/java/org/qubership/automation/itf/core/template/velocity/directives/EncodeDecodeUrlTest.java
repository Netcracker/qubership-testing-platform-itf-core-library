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

import static org.testng.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

public class EncodeDecodeUrlTest {

    private static final DecodeUrl decoder = new DecodeUrl();
    private static final EncodeUrl encoder = new EncodeUrl();

    private static final String expectedContent = "Test content to encodeUrl: \n\t"
            + "Something non-Latin: Günter Александр, "
            + "digits and special chars: "
            + "`1234567890-=[];'\\,./\n"
            + "~!@#$%^&*()_+{}:\"|<>?";
    private static final String expectedEncodedContent = "Test+content+to+encodeUrl%3A+%0A%09Something+non-Latin"
            + "%3A+G%C3%BCnter+%D0%90%D0%BB%D0%B5%D0%BA%D1%81%D0%B0%D0%BD%D0%B4%D1%80%2C+digits+and+special+chars"
            + "%3A+%601234567890-%3D%5B%5D%3B%27%5C%2C.%2F%0A%7E%21%40%23%24%25%5E%26*%28%29_"
            + "%2B%7B%7D%3A%22%7C%3C%3E%3F";

    @Test
    public void testEncodeContent() {
        String encoded = encoder.encodeUrl(expectedContent, "UTF-8");
        assertEquals(encoded, expectedEncodedContent);
    }

    @Test
    public void testDecodeContent() {
        String decoded = decoder.decodeUrl(expectedEncodedContent, "UTF-8");
        assertEquals(decoded, expectedContent);
    }

    @Test
    public void testEncodeEmptyContent() {
        String encoded = encoder.encodeUrl(StringUtils.EMPTY, "UTF-8");
        assertEquals(encoded, StringUtils.EMPTY, "Empty string should be encoded to Empty string");
        encoded = encoder.encodeUrl(null, "UTF-8");
        assertEquals(encoded, StringUtils.EMPTY, "Null should be encoded to Empty string");
    }

    @Test
    public void testDecodeEmptyContent() {
        String decoded = decoder.decodeUrl(StringUtils.EMPTY, "UTF-8");
        assertEquals(decoded, StringUtils.EMPTY, "Empty string should be decoded to Empty string");
        decoded = decoder.decodeUrl(null, "UTF-8");
        assertEquals(decoded, StringUtils.EMPTY, "Null should be decoded to Empty string");
    }

    @Test
    public void testDecodeEncodeWithInvalidEncoding() {
        String encoding = "iso 8859 1";
        String result = decoder.decodeUrl(expectedEncodedContent, encoding);
        assertEquals(result, "Unsupported encoding [" + encoding + "] for decodeUrl directive",
                "In case invalid 'encoding' parameter, result should be the exception message");
        result = encoder.encodeUrl(expectedContent, encoding);
        assertEquals(result, "Unsupported encoding [" + encoding + "] for encodeUrl directive",
                "In case invalid 'encoding' parameter, result should be the exception message");
    }
}
