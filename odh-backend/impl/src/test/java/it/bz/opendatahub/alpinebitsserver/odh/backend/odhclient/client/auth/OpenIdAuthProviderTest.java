// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.auth;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link OpenIdAuthProvider}.
 */
public class OpenIdAuthProviderTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testBuilder_ShouldThrow_WhenAuthUrlIsNull() {
        new OpenIdAuthProvider.Builder(null, "some", "value");
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testAuthenticate_ShouldThrow_OnUrlFormatError() {
        OpenIdAuthProvider openIdAuthProvider = new OpenIdAuthProvider.Builder("invalid_url", "some", "value").build();
        openIdAuthProvider.authenticate();
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void testAuthenticate_ShouldThrow_OnAuthenticationError() throws IOException {
        HttpServer server = buildServer(exchange -> {
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();

        String serverUrl = getServerUrl(server);

        OpenIdAuthProvider openIdAuthProvider = new OpenIdAuthProvider.Builder(serverUrl, "some", "value").build();
        openIdAuthProvider.authenticate();
    }

    @Test
    public void testAuthenticate_ShouldReturnAuthentication() throws IOException {
        String expectedToken = "123";

        HttpServer server = buildServer(exchange -> {
            String response = "{\"access_token\":\"" + expectedToken + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.start();

        String serverUrl = getServerUrl(server);

        OpenIdAuthProvider openIdAuthProvider = new OpenIdAuthProvider.Builder(serverUrl, "some", "value").build();
        String token = openIdAuthProvider.authenticate();
        assertEquals(token, expectedToken);

        server.stop(0);
    }

    private HttpServer buildServer(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", handler);
        return server;
    }

    private String getServerUrl(HttpServer server) {
        int port = server.getAddress().getPort();
        return "http://localhost:" + port;
    }
}