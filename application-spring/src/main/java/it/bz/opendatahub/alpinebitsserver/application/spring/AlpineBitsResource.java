// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.application.spring;

import it.bz.opendatahub.alpinebits.common.utils.middleware.ComposingMiddlewareBuilder;
import it.bz.opendatahub.alpinebits.middleware.Context;
import it.bz.opendatahub.alpinebits.middleware.Middleware;
import it.bz.opendatahub.alpinebits.servlet.ContextBuilder;
import it.bz.opendatahub.alpinebits.servlet.impl.AlpineBitsServlet;
import it.bz.opendatahub.alpinebits.servlet.impl.DefaultContextBuilder;
import it.bz.opendatahub.alpinebits.servlet.impl.DefaultRequestExceptionHandler;
import it.bz.opendatahub.alpinebits.servlet.middleware.AlpineBitsClientProtocolMiddleware;
import it.bz.opendatahub.alpinebits.servlet.middleware.BasicAuthenticationMiddleware;
import it.bz.opendatahub.alpinebits.servlet.middleware.ContentTypeHintMiddleware;
import it.bz.opendatahub.alpinebits.servlet.middleware.GzipUnsupportedMiddleware;
import it.bz.opendatahub.alpinebits.servlet.middleware.StatisticsMiddleware;
import it.bz.opendatahub.alpinebitsserver.application.common.routing.RoutingMiddlewareProvider;
import it.bz.opendatahub.alpinebitsserver.application.spring.middleware.MultipartFormExtractorMiddleware;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.middleware.OdhBackendServiceProvidingMiddleware;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * This REST controller provides an AlpineBits endpoint.
 */
@RestController
public class AlpineBitsResource {

    private final ContextBuilder contextBuilder = new DefaultContextBuilder();
    private final DefaultRequestExceptionHandler defaultRequestExceptionHandler = new DefaultRequestExceptionHandler();

    private Middleware middleware;

    @Value("${odh.url}")
    private String odhUrl;

    @Value("${odh.auth.url}")
    private String odhAuthUrl;

    @Value("${odh.auth.client.id}")
    private String odhAuthClientId;

    @Value("${odh.auth.client.secret}")
    private String odhAuthClientSecret;

    @PostConstruct
    public void initMiddleware() {
        this.middleware = ComposingMiddlewareBuilder.compose(Arrays.asList(
                new StatisticsMiddleware(),
                new ContentTypeHintMiddleware(),
                new AlpineBitsClientProtocolMiddleware(),
                new BasicAuthenticationMiddleware(),
                new GzipUnsupportedMiddleware(),
                new MultipartFormExtractorMiddleware(),
                new OdhBackendServiceProvidingMiddleware(this.odhUrl, this.odhAuthUrl, this.odhAuthClientId, this.odhAuthClientSecret),
                RoutingMiddlewareProvider.buildRoutingMiddleware()
        ));
    }

    @PostMapping("${alpinebits.path}")
    public void alpineBitsEntryPoint(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestId = UUID.randomUUID().toString();
            request.setAttribute(AlpineBitsServlet.REQUEST_ID, requestId);
            MDC.put(AlpineBitsServlet.REQUEST_ID, requestId);

            Context ctx = contextBuilder.fromRequest(request, response, requestId);
            this.middleware.handleContext(ctx, null);
            // Disable checkstyle for this catch-all case
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            this.defaultRequestExceptionHandler.handleRequestException(request, response, e);
        }
    }

}
