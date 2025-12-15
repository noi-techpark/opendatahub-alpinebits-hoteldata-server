/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import jakarta.xml.bind.JAXBElement;

import java.io.IOException;

/**
 * Serialize {@link JAXBElement} instances into JSON data.
 * <p>
 * The serialized JSON contains the {@link JAXBElement#getValue()} and type
 * information taken from {@link JAXBElement#getDeclaredType()} to simplify
 * the deserialization.
 */
// Suppress "rawtypes" warning because the JsonDeserializer type is the generic JAXBElement which triggers a warning
@SuppressWarnings("rawtypes")
public class JAXBElementSerializer extends JsonSerializer<JAXBElement> {

    private final ObjectMapper mapper;

    public JAXBElementSerializer() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JakartaXmlBindAnnotationModule());
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void serialize(JAXBElement value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Convert JAXBElement value
        ObjectNode node = mapper.valueToTree(value.getValue());
        // Add property that contains type (for later deserialization)
        node.put("_declaredType", value.getDeclaredType().getCanonicalName());
        // Write node
        gen.writeTree(node);
    }

    @Override
    public void serializeWithType(JAXBElement value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        this.serialize(value, gen, serializers);
    }
}
