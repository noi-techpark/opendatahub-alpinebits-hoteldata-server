/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.serialization;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.xml.bind.JAXBElement;

import java.io.Serial;

/**
 * Custom Jackson serializer / deserializer module for ODH support.
 */
public class OtaJaxbModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 1L;

    public OtaJaxbModule() {
        super(PackageVersion.VERSION);

        // Register serializers
        addSerializer(JAXBElement.class, new JAXBElementSerializer());

        // Register deserializers
        addDeserializer(JAXBElement.class, new JAXBElementDeserializer());
    }
}
