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
import it.bz.opendatahub.alpinebits.servlet.middleware.HeaderExtractingMiddleware;
import it.bz.opendatahub.alpinebits.xml.JAXBObjectToXmlConverter;
import it.bz.opendatahub.alpinebits.xml.JAXBXmlToObjectConverter;
import it.bz.opendatahub.alpinebits.xml.ObjectToXmlConverter;
import it.bz.opendatahub.alpinebits.xml.XmlToObjectConverter;
import it.bz.opendatahub.alpinebits.xml.XmlValidationSchemaProvider;
import it.bz.opendatahub.alpinebits.xml.middleware.XmlRequestMappingMiddleware;
import it.bz.opendatahub.alpinebits.xml.middleware.XmlResponseMappingMiddleware;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRQ;
import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.ActionExceptionHandler;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeExtractor;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.HotelCodeMissingChecker;
import it.bz.opendatahub.alpinebitsserver.application.common.utils.XmlMiddlewareBuilder;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.InventoryPullMiddleware;

import javax.xml.validation.Schema;
import java.util.Arrays;

/**
 * Utility class to build a {@link InventoryPullMiddleware} for unauthenticated requests.
 */
public final class UnauthenticatedInventoryPullMiddlewareBuilder {

    private static final String ALPINE_BITS_VERSION = AlpineBitsVersion.V_2024_10;

    private static final String WITH_EXTENDED_SERVICE_CODES_XSD = "alpinebits-2024-10-with-extended-hotelinfo-service-codes.xsd";

    private static final String WITH_EXTENDED_HOTELINFO_SERVICE_CODES_HEADER = "X-With-Extended-Hotelinfo-Service-Codes";
    private static final Key<String> WITH_EXTENDED_HOTELINFO_SERVICE_CODES_KEY = Key.key("with-extended-hotelinfo-service-codes", String.class);

    private static final Key<OTAHotelDescriptiveInfoRQ> OTA_INVENTORY_PULL_REQUEST
            = Key.key("inventory pull request", OTAHotelDescriptiveInfoRQ.class);
    private static final Key<OTAHotelDescriptiveInfoRS> OTA_INVENTORY_PULL_RESPONSE
            = Key.key("inventory pull response", OTAHotelDescriptiveInfoRS.class);

    private static final Middleware REGULAR_XML_TO_OBJECT_MIDDLEWARE =
            XmlMiddlewareBuilder.buildXmlToObjectConvertingMiddleware(OTA_INVENTORY_PULL_REQUEST, ALPINE_BITS_VERSION);
    private static final Middleware REGULAR_OBJECT_TO_XML_MIDDLEWARE =
            XmlMiddlewareBuilder.buildObjectToXmlConvertingMiddleware(OTA_INVENTORY_PULL_RESPONSE, ALPINE_BITS_VERSION);

    private static final Middleware LOOSE_XML_TO_OBJECT_MIDDLEWARE =
            buildXmlToObjectConvertingMiddlewareWithSchema(OTA_INVENTORY_PULL_REQUEST, WITH_EXTENDED_SERVICE_CODES_XSD);
    private static final Middleware LOOSE_OBJECT_TO_XML_MIDDLEWARE =
            buildObjectToXmlConvertingMiddlewareWithSchema(OTA_INVENTORY_PULL_RESPONSE, WITH_EXTENDED_SERVICE_CODES_XSD);

    private static final Middleware XML_SWITCH_MIDDLEWARE = (ctx, chain) -> {
        boolean withExtendedHotelInfoServiceCodesKey = ctx.contains(WITH_EXTENDED_HOTELINFO_SERVICE_CODES_KEY);

        Middleware xmlToObjectMiddleware = withExtendedHotelInfoServiceCodesKey ? LOOSE_XML_TO_OBJECT_MIDDLEWARE : REGULAR_XML_TO_OBJECT_MIDDLEWARE;
        Middleware objectToXmlMiddleware = withExtendedHotelInfoServiceCodesKey ? LOOSE_OBJECT_TO_XML_MIDDLEWARE : REGULAR_OBJECT_TO_XML_MIDDLEWARE;

        xmlToObjectMiddleware.handleContext(ctx, chain);
        chain.next();
        objectToXmlMiddleware.handleContext(ctx, chain);
    };

    private UnauthenticatedInventoryPullMiddlewareBuilder() {
        // Empty
    }

    public static Middleware buildInventoryPullMiddlewareWithNoAuthentication() {
        return ComposingMiddlewareBuilder.compose(Arrays.asList(
                new ActionExceptionHandler<>(ALPINE_BITS_VERSION, OTA_INVENTORY_PULL_RESPONSE, ResponseOutcomeBuilder::forOTAHotelDescriptiveInfoRS),
                new HeaderExtractingMiddleware(WITH_EXTENDED_HOTELINFO_SERVICE_CODES_HEADER, WITH_EXTENDED_HOTELINFO_SERVICE_CODES_KEY),
                XML_SWITCH_MIDDLEWARE,
                new HotelCodeMissingChecker<>(
                        OTA_INVENTORY_PULL_REQUEST,
                        OTA_INVENTORY_PULL_RESPONSE,
                        HotelCodeExtractor::hasHotelCode,
                        ResponseOutcomeBuilder::forOTAHotelDescriptiveInfoRS
                ),
                new InventoryHotelInfoPullAdapter(WITH_EXTENDED_HOTELINFO_SERVICE_CODES_KEY),
                new InventoryPullMiddleware(
                        OTA_INVENTORY_PULL_REQUEST,
                        OTA_INVENTORY_PULL_RESPONSE,
                        WITH_EXTENDED_HOTELINFO_SERVICE_CODES_KEY
                )
        ));
    }

    private static <T> Middleware buildXmlToObjectConvertingMiddlewareWithSchema(Key<T> key, String filename) {
        Schema schema = XmlValidationSchemaProvider.buildXsdSchema(filename);
        XmlToObjectConverter<T> converter = new JAXBXmlToObjectConverter.Builder<>(key.getType()).schema(schema).build();
        return new XmlRequestMappingMiddleware<>(converter, key);
    }

    private static <T> Middleware buildObjectToXmlConvertingMiddlewareWithSchema(Key<T> key, String filename) {
        Schema schema = XmlValidationSchemaProvider.buildXsdSchema(filename);
        ObjectToXmlConverter converter = new JAXBObjectToXmlConverter.Builder()
                .schema(schema)
                .prettyPrint(true)
                .build();
        return new XmlResponseMappingMiddleware<>(converter, key);
    }

}
