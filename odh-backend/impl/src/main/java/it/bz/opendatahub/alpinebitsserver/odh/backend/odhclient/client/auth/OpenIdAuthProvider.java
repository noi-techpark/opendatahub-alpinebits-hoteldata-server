// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.auth;

import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.ApiKeyResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

import java.util.Objects;

/**
 * Implementation of {@link AuthProvider} for OpenID.
 */
public final class OpenIdAuthProvider implements AuthProvider<String> {

    // The Sonar warning "'PASSWORD' detected in this expression" can be ignored
    @SuppressWarnings({"squid:S2068"})
    private static final String PASSWORD_PARAM_NAME = "password";
    private static final String USERNAME_PARAM_NAME = "username";
    private static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    private static final String CLIENT_ID_PARAM_NAME = "client_id";
    private static final String CLIENT_ID_SECRET = "client_secret";

    private static final String GRANT_TYPE = "password";

    private final Invocation.Builder invocationBuilder;
    private final MultivaluedHashMap<String, String> formData;

    private OpenIdAuthProvider(String url, String clientId, String clientSecret, String username, String password) {
        // Configure reusable client invocation builder
        Client client = ClientBuilder.newClient();
        invocationBuilder = client.target(url).request(MediaType.APPLICATION_JSON);

        // Configure reusable form data
        formData = new MultivaluedHashMap<>();
        formData.add(GRANT_TYPE_PARAM_NAME, GRANT_TYPE);
        formData.add(CLIENT_ID_PARAM_NAME, clientId);
        formData.add(CLIENT_ID_SECRET, clientSecret);
        formData.add(USERNAME_PARAM_NAME, username);
        formData.add(PASSWORD_PARAM_NAME, password);
    }

    @Override
    // Suppress Checkstyle warning about the catch-all block
    @SuppressWarnings("IllegalCatch")
    public String authenticate() {
        try {
            return invocationBuilder
                    .post(Entity.form(formData), ApiKeyResponse.class)
                    .getAccessToken();
        } catch (Exception e) {
            throw new AuthenticationException("OpenID authentication error: " + e.getMessage(), e);
        }
    }

    /**
     * Builder for {@link OpenIdAuthProvider}.
     */
    public static final class Builder {
        private final String url;
        private final String clientId;
        private final String clientSecret;
        private String username;
        private String password;

        public Builder(String url, String clientId, String clientSecret) {
            Objects.requireNonNull(url, "OpenID auth URL must not be null");

            this.url = url;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public OpenIdAuthProvider build() {
            return new OpenIdAuthProvider(url, clientId, clientSecret, username, password);
        }
    }
}
