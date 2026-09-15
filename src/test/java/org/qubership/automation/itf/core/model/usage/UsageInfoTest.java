/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.model.usage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.common.Storable;

/**
 * Covers {@link UsageInfo#getPath()}.
 *
 * <p>The loop used to re-read {@code referer.getParent()} on every iteration instead of advancing
 * to {@code parent.getParent()}, so it never reached a {@code null} parent and looped forever
 * whenever {@code referer} had more than one ancestor. It only appeared to work for a referer with
 * at most one ancestor, which is the case every caller in this codebase happened to exercise.</p>
 */
class UsageInfoTest {

    @Test
    void getPathReturnsEveryAncestorRootFirst() {
        Storable root = mock(Storable.class);
        Storable middle = mock(Storable.class);
        Storable immediateParent = mock(Storable.class);
        Storable referer = mock(Storable.class);
        when(referer.getParent()).thenReturn(immediateParent);
        when(immediateParent.getParent()).thenReturn(middle);
        when(middle.getParent()).thenReturn(root);
        when(root.getParent()).thenReturn(null);

        UsageInfo usageInfo = new UsageInfo();
        usageInfo.setReferer(referer);

        Storable[] path = assertTimeoutPreemptively(Duration.ofSeconds(2), usageInfo::getPath,
                "getPath() must terminate for a referer with more than one ancestor");

        assertArrayEquals(new Storable[] {root, middle, immediateParent}, path);
    }

    @Test
    void getPathReturnsEmptyArrayWhenRefererHasNoParent() {
        Storable referer = mock(Storable.class);
        when(referer.getParent()).thenReturn(null);

        UsageInfo usageInfo = new UsageInfo();
        usageInfo.setReferer(referer);

        assertArrayEquals(new Storable[0], usageInfo.getPath());
    }

    @Test
    void getPathCachesTheComputedArray() {
        Storable immediateParent = mock(Storable.class);
        Storable referer = mock(Storable.class);
        when(referer.getParent()).thenReturn(immediateParent);
        when(immediateParent.getParent()).thenReturn(null);

        UsageInfo usageInfo = new UsageInfo();
        usageInfo.setReferer(referer);

        Storable[] first = usageInfo.getPath();
        Storable[] second = usageInfo.getPath();

        assertSame(first, second);
    }
}
