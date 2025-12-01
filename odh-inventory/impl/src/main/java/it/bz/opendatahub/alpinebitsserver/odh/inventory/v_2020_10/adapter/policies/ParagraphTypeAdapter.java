// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2020_10.adapter.policies;

import it.bz.opendatahub.alpinebits.xml.schema.ota.FormattedTextTextType;
import it.bz.opendatahub.alpinebits.xml.schema.ota.ParagraphType;
import jakarta.xml.bind.JAXBElement;

import java.util.List;

/**
 * This adapter is used to remove elements from {@link ParagraphType} elements that are not
 * allowed in AlpineBits 2020-10.
 */
public final class ParagraphTypeAdapter {

    private ParagraphTypeAdapter() {
        // Empty
    }

    public static void removeUnsupported(ParagraphType paragraphType) {
        paragraphType.setCreateDateTime(null);
        paragraphType.setCreatorID(null);
        paragraphType.setLanguage(null);
        paragraphType.setLastModifierID(null);
        paragraphType.setLastModifyDateTime(null);
        paragraphType.setName(null);
        paragraphType.setParagraphNumber(null);
        paragraphType.setPurgeDate(null);

        // Cleanup Text element
        if (paragraphType.getTextsAndImagesAndURLS() != null) {
            List<JAXBElement<?>> result = paragraphType.getTextsAndImagesAndURLS().stream()
                    .filter(jaxbElement -> jaxbElement.getValue() instanceof FormattedTextTextType)
                    .toList();

            result.forEach(jaxbElement -> ((FormattedTextTextType) jaxbElement.getValue()).setFormatted(null));

            paragraphType.getTextsAndImagesAndURLS().clear();
            paragraphType.getTextsAndImagesAndURLS().addAll(result);
        }
    }
}
