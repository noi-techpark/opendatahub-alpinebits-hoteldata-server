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
import it.bz.opendatahub.alpinebits.validation.Validator;
import it.bz.opendatahub.alpinebits.validation.context.ValidationContextProvider;
import it.bz.opendatahub.alpinebits.validation.context.inventory.InventoryContext;
import it.bz.opendatahub.alpinebits.validation.context.inventory.InventoryContextProvider;
import it.bz.opendatahub.alpinebits.validation.middleware.ValidationMiddleware;
import it.bz.opendatahub.alpinebits.validation.schema.v_2024_10.inventory.OTAHotelDescriptiveContentNotifRQValidator;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveContentNotifRQ;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveContentNotifRS;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.ActionExceptionHandler;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeExtractor;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeMissingChecker;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.XmlMiddlewareBuilder;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.InventoryPushMiddleware;

import java.util.Arrays;

/**
 * Utility class to build a {@link InventoryPushMiddleware}.
 */
public final class InventoryPushMiddlewareBuilder {

    private static final String ALPINE_BITS_VERSION = AlpineBitsVersion.V_2024_10;
    private static final Key<OTAHotelDescriptiveContentNotifRQ> OTA_INVENTORY_PUSH_REQUEST
            = Key.key("inventory push request", OTAHotelDescriptiveContentNotifRQ.class);
    private static final Key<OTAHotelDescriptiveContentNotifRS> OTA_INVENTORY_PUSH_RESPONSE
            = Key.key("inventory push request", OTAHotelDescriptiveContentNotifRS.class);

    private InventoryPushMiddlewareBuilder() {
        // Empty
    }

    public static Middleware buildInventoryPushMiddleware() {
        return ComposingMiddlewareBuilder.compose(Arrays.asList(
                new ActionExceptionHandler<>(ALPINE_BITS_VERSION, OTA_INVENTORY_PUSH_RESPONSE, ResponseOutcomeBuilder::forOTAHotelDescriptiveContentNotifRS),
                XmlMiddlewareBuilder.buildXmlToObjectConvertingMiddleware(OTA_INVENTORY_PUSH_REQUEST, ALPINE_BITS_VERSION),
                XmlMiddlewareBuilder.buildObjectToXmlConvertingMiddleware(OTA_INVENTORY_PUSH_RESPONSE, ALPINE_BITS_VERSION),
                new HotelCodeMissingChecker<>(
                        OTA_INVENTORY_PUSH_REQUEST,
                        OTA_INVENTORY_PUSH_RESPONSE,
                        HotelCodeExtractor::hasHotelCode,
                        ResponseOutcomeBuilder::forOTAHotelDescriptiveContentNotifRS
                ),
                buildValidationMiddleware(),
                new InventoryPushMiddleware(
                        OTA_INVENTORY_PUSH_REQUEST,
                        OTA_INVENTORY_PUSH_RESPONSE
                )
        ));
    }

    private static Middleware buildValidationMiddleware() {
        Validator<OTAHotelDescriptiveContentNotifRQ, InventoryContext> validator = new OTAHotelDescriptiveContentNotifRQValidator();
        ValidationContextProvider<InventoryContext> validationContextProvider = new InventoryContextProvider();
        return new ValidationMiddleware<>(OTA_INVENTORY_PUSH_REQUEST, validator, validationContextProvider);
    }

}
