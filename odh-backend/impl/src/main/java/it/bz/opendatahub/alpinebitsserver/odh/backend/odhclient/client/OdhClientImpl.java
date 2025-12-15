// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.auth.AuthProvider;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.auth.AuthenticationException;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.serialization.OtaJaxbModule;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import java.util.Map;

/**
 * The client provides methods to access ODH. It is intended for
 * internal usage.
 * <p>
 * Note that the provided username and password are kept in-memory,
 * since the client may need to request new ODH API keys at any
 * time. This may pose a security risk. Please take that into
 * consideration when using this class.
 */
public class OdhClientImpl implements AuthenticatedOdhClient {

    private final WebTarget webTarget;
    private final String apiKey;

    private boolean isAuthenticated;

    public OdhClientImpl(String baseUrl, AuthProvider<String> authProvider) {
        Client client = this.buildClient();
        this.webTarget = client.target(baseUrl);

        try {
            this.apiKey = authProvider.authenticate();
            this.isAuthenticated = true;
        } catch (AuthenticationException e) {
            this.isAuthenticated = false;
            throw new AuthenticationException("invalid or missing username/password", e);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * Fetch the resource of type <code>T</code> from ODH.
     * <p>
     * The resource is fetched from the given <code>path</code> using the specified
     * HTTP <code>method</code>. The <code>queryParams</code> are appended to the
     * request URL, the <code>body</code> is used as request body.
     *
     * @param path        this path is used to fetch the resource
     * @param method      use this HTTP request method, e.g. GET, POST, PUT, ...
     * @param queryParams queryParams are appended to the URL
     * @param body        this {@link Entity} is send as request body
     * @param genericType the genericType is useful for more advanced use cases, e.g. when
     *                    the result is expected to be a List of type <code>T</code>. Take
     *                    a look at {@link #fetch(String, String, Map, Entity, Class)} if you just want to
     *                    provide a class as type parameter
     * @param <T>         result type
     * @return the response body as instance of <code>T</code> if successful
     */
    @Override
    public <T> T fetch(String path, String method, Map<String, String> queryParams, Entity<?> body, GenericType<T> genericType) {
        try (Response response = this.fetchInternal(path, method, queryParams, body)) {
            if (response.getStatus() >= 400) {
                throw new WebApplicationException(response.getStatusInfo().toEnum());
            }
            return response.readEntity(genericType);
        }
    }

    /**
     * Fetch the resource of type <code>T</code> from ODH.
     * <p>
     * The resource is fetched from the given <code>path</code> using the specified
     * HTTP <code>method</code>. The <code>queryParams</code> are appended to the
     * request URL, the <code>body</code> is used as request body.
     * <p>
     *
     * @param path        this path is used to fetch the resource
     * @param method      use this HTTP request method, e.g. GET, POST, PUT, ...
     * @param queryParams queryParams are appended to the URL
     * @param body        this {@link Entity} is send as request body
     * @param resultClass the resultClass defines the class of the result type. Take
     *                    a look at {@link #fetch(String, String, Map, Entity, GenericType)} for more
     *                    complex cases e.g. List of type <code>T</code>
     * @param <T>         result type
     * @return the response body as instance of <code>T</code> if successful
     */
    @Override
    public <T> T fetch(String path, String method, Map<String, String> queryParams, Entity<?> body, Class<T> resultClass) {
        try (Response response = this.fetchInternal(path, method, queryParams, body)) {
            if (response.getStatus() >= 400) {
                throw new WebApplicationException(response.getStatusInfo().toEnum());
            }
            return response.readEntity(resultClass);
        }
    }

    private Client buildClient() {
        ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // Add module to support the Java Date/Time API
        om.registerModule(new JavaTimeModule());

        // Add module for better JAXB support
        om.registerModule(new JakartaXmlBindAnnotationModule());

        // Add serialization module for AlpineBits 2020-10 (JAXBElement serializations)
        om.registerModule(new OtaJaxbModule());

        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);

        ClientConfig config = new ClientConfig(provider);

        return ClientBuilder.newClient(config);
    }

    /**
     * Fetch the resource from the given <code>path</code>, appending <code>queryParams</code>
     * to the request URL.
     *
     * @param path        this path is used to fetch the resource
     * @param queryParams are appended to the URL
     * @return {@link Response} that can be further handled
     */
    private Response fetchInternal(String path, String method, Map<String, String> queryParams, Entity<?> body) {
        WebTarget fetchTarget = this.buildFetchTarget(path, queryParams);
        Invocation.Builder request = fetchTarget.request(MediaType.APPLICATION_JSON);
        Invocation.Builder builder = request.header("Authorization", "Bearer " + this.apiKey);
        return builder.method(method, body);
    }

    private WebTarget buildFetchTarget(String path, Map<String, String> queryParamsMap) {
        WebTarget fetchTarget = this.webTarget.path(path);

        if (queryParamsMap != null) {
            for (Map.Entry<String, String> entry : queryParamsMap.entrySet()) {
                fetchTarget = fetchTarget.queryParam(entry.getKey(), entry.getValue());
            }
        }

        return fetchTarget;
    }
}
