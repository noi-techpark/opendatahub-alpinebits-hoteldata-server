// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2024_10;

import it.bz.opendatahub.alpinebits.common.constants.AlpineBitsVersion;
import it.bz.opendatahub.alpinebits.common.utils.middleware.ComposingMiddlewareBuilder;
import it.bz.opendatahub.alpinebits.common.utils.response.ResponseOutcomeBuilder;
import it.bz.opendatahub.alpinebits.middleware.Key;
import it.bz.opendatahub.alpinebits.middleware.Middleware;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRQ;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.ActionExceptionHandler;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeExtractor;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeMissingChecker;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.XmlMiddlewareBuilder;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.AuthenticatedInventoryPullMiddleware;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.AuthenticationBasedRoutingMiddleware;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.InventoryPullMiddleware;

import java.util.Arrays;

/**
 * Utility class to build a {@link InventoryPullMiddleware}.
 */
public final class InventoryPullMiddlewareBuilder {

    private static final String ALPINE_BITS_VERSION = AlpineBitsVersion.V_2024_10;
    private static final Key<OTAHotelDescriptiveInfoRQ> OTA_INVENTORY_PULL_REQUEST
            = Key.key("inventory pull request", OTAHotelDescriptiveInfoRQ.class);
    private static final Key<OTAHotelDescriptiveInfoRS> OTA_INVENTORY_PULL_RESPONSE
            = Key.key("inventory pull response", OTAHotelDescriptiveInfoRS.class);

    private InventoryPullMiddlewareBuilder() {
        // Empty
    }

    public static Middleware buildInventoryPullMiddleware() {
        // Return a middleware, that invokes a different middleware, based
        // on the validity of the credentials provided with the AlpineBits request.
        return new AuthenticationBasedRoutingMiddleware(
                buildInventoryPullMiddlewareWithAuthentication(),
                UnauthenticatedInventoryPullMiddlewareBuilder.buildInventoryPullMiddlewareWithNoAuthentication()
        );
    }

    private static Middleware buildInventoryPullMiddlewareWithAuthentication() {
        return ComposingMiddlewareBuilder.compose(Arrays.asList(
                new ActionExceptionHandler<>(ALPINE_BITS_VERSION, OTA_INVENTORY_PULL_RESPONSE, ResponseOutcomeBuilder::forOTAHotelDescriptiveInfoRS),
                XmlMiddlewareBuilder.buildXmlToObjectConvertingMiddleware(OTA_INVENTORY_PULL_REQUEST, ALPINE_BITS_VERSION),
                XmlMiddlewareBuilder.buildObjectToXmlConvertingMiddleware(OTA_INVENTORY_PULL_RESPONSE, ALPINE_BITS_VERSION),
                new HotelCodeMissingChecker<>(
                        OTA_INVENTORY_PULL_REQUEST,
                        OTA_INVENTORY_PULL_RESPONSE,
                        HotelCodeExtractor::hasHotelCode,
                        ResponseOutcomeBuilder::forOTAHotelDescriptiveInfoRS
                ),
                new InventoryHotelInfoPullAdapter(null),
                new AuthenticatedInventoryPullMiddleware(
                        OTA_INVENTORY_PULL_REQUEST,
                        OTA_INVENTORY_PULL_RESPONSE
                )
        ));
    }

}
