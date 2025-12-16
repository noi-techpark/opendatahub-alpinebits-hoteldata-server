// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.application.spring.middleware;

import it.bz.opendatahub.alpinebits.common.constants.AlpineBitsAction;
import it.bz.opendatahub.alpinebits.common.context.RequestContextKey;
import it.bz.opendatahub.alpinebits.common.exception.AlpineBitsException;
import it.bz.opendatahub.alpinebits.middleware.Context;
import it.bz.opendatahub.alpinebits.middleware.Middleware;
import it.bz.opendatahub.alpinebits.middleware.MiddlewareChain;
import it.bz.opendatahub.alpinebits.servlet.ServletContextKey;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * This middleware extracts the multipart/form-data from the request.
 */
public class MultipartFormExtractorMiddleware implements Middleware {

    // List of actions that do not require a "request" part
    private final List<String> actionsWithoutRequest = Arrays.asList(
            AlpineBitsAction.GET_VERSION,
            AlpineBitsAction.GET_CAPABILITIES
    );

    @Override
    public void handleContext(Context ctx, MiddlewareChain chain) {
        try {
            HttpServletRequest httpServletRequest = ctx.getOrThrow(ServletContextKey.SERVLET_REQUEST);

            Part actionPart = httpServletRequest.getPart("action");
            if (actionPart == null) {
                throw new AlpineBitsException("unknown or missing action", 400);
            }
            String action = IOUtils.toString(actionPart.getInputStream(), StandardCharsets.UTF_8);
            ctx.put(RequestContextKey.REQUEST_ACTION, action);

            Part requestPart = httpServletRequest.getPart("request");

            // Check if the action requires a request part
            if (requestPart == null && !actionsWithoutRequest.contains(action)) {
                throw new AlpineBitsException("unknown or missing \"request\" param", 400);
            }

            if (requestPart != null) {
                InputStream request = requestPart.getInputStream();
                ctx.put(RequestContextKey.REQUEST_CONTENT_STREAM, request);
            }

            chain.next();
        } catch (IOException | ServletException e) {
            throw new AlpineBitsException("Error while parsing multipart/form-data", 500, e);
        }
    }
}
