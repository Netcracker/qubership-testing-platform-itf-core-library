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

package org.qubership.automation.itf.core.util.helper;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.IDToken;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.springframework.stereotype.Component;

@Component("securityHelper")
public class SecurityHelper {

    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 512;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    /**
     * Deprecated. This method used in monolith ITF with custom ITF authorization.
     */
    @Deprecated
    public static byte[] getSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Deprecated. This method used in monolith ITF with custom ITF authorization.
     */
    @Deprecated
    public static boolean arePasswordsEquals(String testedPassword, byte[] storedPassword, byte[] salt) {
        byte[] hashedTestedPassword = encodePassword(testedPassword.toCharArray(), salt);
        if (hashedTestedPassword.length != storedPassword.length) {
            return false;
        }
        for (int i = 0; i < hashedTestedPassword.length; i++) {
            if (hashedTestedPassword[i] != storedPassword[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deprecated. This method used in monolith ITF with custom ITF authorization.
     */
    @Deprecated
    public static byte[] encodePassword(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Temporary solution to get ProjectUUID from our internal projectId
     * We use it in one place only - ProjectController#getById (itf-executor), when
     * atp-itf-configurator load main UI at first time in STANDALONE mode ONLY.
     * After atp-itf-configurator become FE only (without BE and spring) we will don't need this method and class.
     *
     * @param projectId projectId
     * @return projectUuid
     */
    public static UUID getCurrentProjectUuid(BigInteger projectId) {
        return CoreObjectManager.getInstance().getManager(StubProject.class).getById(projectId).getUuid();
    }

    /**
     * Deprecated. This method used in monolith ITF with custom ITF authorization.
     */
    @Deprecated
    public static IDToken getTokenForLogger(KeycloakPrincipal principal) {
        IDToken idToken = principal.getKeycloakSecurityContext().getIdToken();
        if (idToken == null) {
            idToken = principal.getKeycloakSecurityContext().getToken();
        }
        return idToken;
    }
}
