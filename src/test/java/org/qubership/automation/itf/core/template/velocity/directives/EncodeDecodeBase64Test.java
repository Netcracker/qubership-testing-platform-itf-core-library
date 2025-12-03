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

public class EncodeDecodeBase64Test {

    private static final DecodeBase64 decoder = new DecodeBase64();
    private static final EncodeBase64 encoder = new EncodeBase64();

    private static final String expectedContent = "Test content to encode base64.\n"
            + "ABCDEF GHIJKL MNOPQR STUVWX YZ\n"
            + "abcdef ghijkl mnopqr stuvwx yz\n"
            + "АБВГДЕ ЁЖЗИЙК ЛМНОПР СТУФХЦ ЧШЩЪЫЬ ЭЮЯ\n"
            + "абвгде ёжзийк лмнопр стуфхц чшщъыь эюя\n"
            + "`1234567890-=[];'\\,./\n"
            + "~!@#$%^&*()_+{}:\"|<>?";
    private static final String expectedEncodedContent = "VGVzdCBjb250ZW50IHRvIGVuY29kZSBiYXNlNjQuCkFCQ0RFRiBHSElKS0wg"
            + "TU5PUFFSIFNUVVZXWCBZWgphYmNkZWYgZ2hpamtsIG1ub3BxciBzdHV2d3gg"
            + "eXoK0JDQkdCS0JPQlNCVINCB0JbQl9CY0JnQmiDQm9Cc0J3QntCf0KAg0KHQ"
            + "otCj0KTQpdCmINCn0KjQqdCq0KvQrCDQrdCu0K8K0LDQsdCy0LPQtNC1INGR"
            + "0LbQt9C40LnQuiDQu9C80L3QvtC/0YAg0YHRgtGD0YTRhdGGINGH0YjRidGK"
            + "0YvRjCDRjdGO0Y8KYDEyMzQ1Njc4OTAtPVtdOydcLC4vCn4hQCMkJV4mKigp"
            + "Xyt7fToifDw+Pw==";

    @Test
    public void testEncodeContent() {
        String encoded = encoder.encodeContent(expectedContent);
        assertEquals(encoded, expectedEncodedContent);
    }

    @Test
    public void testDecodeContent() {
        String decoded = decoder.decodeContent(expectedEncodedContent);
        assertEquals(decoded, expectedContent);
    }

    @Test
    public void testEncodeEmptyContent() {
        String encoded = encoder.encodeContent(StringUtils.EMPTY);
        assertEquals(encoded, StringUtils.EMPTY, "Empty string should be encoded to Empty string");
        encoded = encoder.encodeContent(null);
        assertEquals(encoded, StringUtils.EMPTY, "Null should be encoded to Empty string");
    }

    @Test
    public void testDecodeEmptyContent() {
        String decoded = decoder.decodeContent(StringUtils.EMPTY);
        assertEquals(decoded, StringUtils.EMPTY, "Empty string should be decoded to Empty string");
        decoded = decoder.decodeContent(null);
        assertEquals(decoded, StringUtils.EMPTY, "Null should be decoded to Empty string");
    }
}