// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.service;

import it.bz.opendatahub.alpinebits.xml.schema.ota.OTAHotelDescriptiveInfoRS;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client.AuthenticatedOdhClient;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.Accommodation;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.AccommodationRoom;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.PullWrapper;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.dto.PushWrapper;
import it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.exception.OdhBackendException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implements {@link OdhBackendService}.
 */
public class OdhBackendServiceImpl implements OdhBackendService {

    private static final String ODH_ERROR_MESSAGE = "ODH client encountered a problem";

    private final AuthenticatedOdhClient odhClient;

    public OdhBackendServiceImpl(AuthenticatedOdhClient odhClient) {
        this.odhClient = odhClient;
    }

    @Override
    public boolean isAuthenticated() {
        return this.odhClient.isAuthenticated();
    }

    @Override
    public Optional<OTAHotelDescriptiveInfoRS> fetchInventoryBasic(String hotelCode) {
        String path = "AlpineBits/InventoryBasic";

        return this.fetchOTAHotelDescriptiveInfoRS(path, hotelCode);
    }

    @Override
    public Optional<OTAHotelDescriptiveInfoRS> fetchInventoryHotelInfo(String hotelCode) {
        String path = "AlpineBits/InventoryHotelInfo";

        return this.fetchOTAHotelDescriptiveInfoRS(path, hotelCode);
    }

    @Override
    // Suppress warning because of catch-all
    @SuppressWarnings("IllegalCatch")
    public Optional<List<AccommodationRoom>> fetchAccommodationRooms(String accoId) {
        String path = "AccommodationRoom";

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accoid", accoId);
        queryParams.put("idsource", "a0r_id");

        try {
            List<AccommodationRoom> accommodationRooms = odhClient.fetch(
                    path,
                    HttpMethod.GET,
                    queryParams,
                    null,
                    new GenericType<>() {
                    }
            );
            return Optional.of(accommodationRooms);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    // Suppress warning because of catch-all
    @SuppressWarnings("IllegalCatch")
    public Optional<Accommodation> fetchAccommodation(String accoId) {
        String path = "Accommodation/" + accoId;
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("idsource", "a0r_id");

        try {
            Accommodation accommodation = odhClient.fetch(path, HttpMethod.GET, queryParams, null, Accommodation.class);
            return Optional.of(accommodation);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    public void pushFreeRooms(PushWrapper pushWrapper) {
        this.push("AlpineBits/FreeRooms", pushWrapper);
    }

    @Override
    public void pushInventoryBasic(PushWrapper pushWrapper) {
        this.push("AlpineBits/InventoryBasic", pushWrapper);
    }

    @Override
    public void pushInventoryHotelInfo(PushWrapper pushWrapper) {
        this.push("AlpineBits/InventoryHotelInfo", pushWrapper);
    }

    // Suppress warning because of catch-all
    @SuppressWarnings("IllegalCatch")
    private void push(String path, PushWrapper pushWrapper) {
        try {
            odhClient.fetch(path, HttpMethod.POST, null, Entity.json(pushWrapper), String.class);
        } catch (Exception e) {
            handleException(e);
        }
    }

    // Suppress warning because of catch-all
    @SuppressWarnings("IllegalCatch")
    private Optional<OTAHotelDescriptiveInfoRS> fetchOTAHotelDescriptiveInfoRS(String path, String hotelCode) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("accoid", hotelCode);
        try {
            List<PullWrapper<OTAHotelDescriptiveInfoRS>> result = odhClient.fetch(
                    path,
                    HttpMethod.GET,
                    queryParams,
                    null,
                    new GenericType<>() {
                    }
            );

            if (result == null || result.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(result.getFirst().getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private <T> Optional<T> handleException(Exception e) {
        if (e instanceof WebApplicationException webApplicationException) {
            if (Response.Status.NOT_FOUND.equals(webApplicationException.getResponse().getStatusInfo())) {
                return Optional.empty();
            }
            throw new OdhBackendException(e.getMessage(), e);
        }
        throw new OdhBackendException(ODH_ERROR_MESSAGE, e);
    }

}
