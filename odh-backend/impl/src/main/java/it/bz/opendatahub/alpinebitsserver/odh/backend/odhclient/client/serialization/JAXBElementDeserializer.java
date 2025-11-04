/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import it.bz.opendatahub.alpinebits.xml.schema.ota.FormattedTextTextType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ObjectFactory;

import javax.xml.bind.JAXBElement;
import java.io.IOException;

/**
 * Deserialize JSON into {@link JAXBElement} instances for {@link FormattedTextTextType}
 * type of data.
 * <p>
 * This deserializer transforms only JSON data of type FormattedTextTextType into JAXBElement instances.
 * All other data types result in a null value, because there is no general way to determine the
 * JAXBElement type from the serialized JSON). But this is a minor issue for AlpineBits 2020-10
 * data, because the only JAXBElements used are of type FormattedTextTextType.
 */
// Suppress "rawtypes" warning because the JsonDeserializer type is the generic JAXBElement which triggers a warning
@SuppressWarnings("rawtypes")
public class JAXBElementDeserializer extends JsonDeserializer<JAXBElement> {

    private static final String FORMATTED_TEXT_TEXT_TYPE_CLASS_NAME = FormattedTextTextType.class.getCanonicalName();

    private final ObjectFactory factory = new ObjectFactory();
    private final ObjectMapper mapper;

    public JAXBElementDeserializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JaxbAnnotationModule());
    }

    @Override
    public JAXBElement<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // Check if there is some type information
        ObjectNode node = p.readValueAsTree();
        JsonNode declaredType = node.get("_declaredType");

        // If type information was found and it is known, deserialize the data
        if (declaredType != null && FORMATTED_TEXT_TEXT_TYPE_CLASS_NAME.equals(declaredType.asText())) {
            FormattedTextTextType formattedTextTextType = mapper.treeToValue(node, FormattedTextTextType.class);
            return factory.createParagraphTypeText(formattedTextTextType);
        }

        // Return null by default
        return null;
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }

}
