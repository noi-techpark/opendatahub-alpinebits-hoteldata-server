// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.application.common.routing;

import it.bz.opendatahub.alpinebits.common.constants.AlpineBitsCapability;
import it.bz.opendatahub.alpinebits.common.constants.AlpineBitsVersion;
import it.bz.opendatahub.alpinebits.handshaking.DefaultContextSerializer;
import it.bz.opendatahub.alpinebits.handshaking.middleware.HandshakingMiddleware;
import it.bz.opendatahub.alpinebits.routing.DefaultRouter;
import it.bz.opendatahub.alpinebits.routing.RoutingBuilder;
import it.bz.opendatahub.alpinebits.routing.constants.Action;
import it.bz.opendatahub.alpinebitsserver.odh.freerooms.HotelInvCountNotifPushMiddlewareBuilder;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2024_10.InventoryPullMiddlewareBuilder;
import it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2024_10.InventoryPushMiddlewareBuilder;

/**
 * Route definitions for AlpineBits 2024-10.
 */
public final class RoutesFor202410 {

    private RoutesFor202410() {
        // Empty
    }

    /**
     * Add 2024-10 routes to the given builder.
     *
     * @param builder The routes will be added to this builder.
     * @return A {@link RoutingBuilder.FinalBuilder} that can be used for further route building.
     */
    public static RoutingBuilder.FinalBuilder routes(DefaultRouter.Builder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("The builder must not be null");
        }
        return builder.version(AlpineBitsVersion.V_2024_10)
                .supportsAction(Action.HANDSHAKING)
                .withCapabilities(AlpineBitsCapability.HANDSHAKING)
                .using(new HandshakingMiddleware(new DefaultContextSerializer(AlpineBitsVersion.V_2024_10)))
                .and()
                .supportsAction(Action.INVENTORY_BASIC_PULL)
                .withCapabilities(AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_INFO_INVENTORY)
                .using(InventoryPullMiddlewareBuilder.buildInventoryPullMiddleware())
                .and()
                .supportsAction(Action.INVENTORY_HOTEL_INFO_PULL)
                .withCapabilities(AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_INFO_INFO)
                .using(InventoryPullMiddlewareBuilder.buildInventoryPullMiddleware())
                .and()
                .supportsAction(Action.INVENTORY_BASIC_PUSH)
                .withCapabilities(
                        AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_CONTENT_NOTIF_INVENTORY,
                        AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_CONTENT_NOTIF_INVENTORY_USE_ROOMS,
                        AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_CONTENT_NOTIF_OCCUPANCY_CHILDREN
                )
                .using(InventoryPushMiddlewareBuilder.buildInventoryPushMiddleware())
                .and()
                .supportsAction(Action.INVENTORY_HOTEL_INFO_PUSH)
                .withCapabilities(AlpineBitsCapability.INVENTORY_HOTEL_DESCRIPTIVE_CONTENT_NOTIF_INFO)
                .using(InventoryPushMiddlewareBuilder.buildInventoryPushMiddleware())
                .and()
                .supportsAction(Action.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_FREE_ROOMS)
                .withCapabilities(
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_ROOMS,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_CATEGORIES,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_OUT_OF_ORDER,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_OUT_OF_MARKET,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_CLOSING_SEASONS,
                        AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_COMPLETE_SET
                )
                .using(HotelInvCountNotifPushMiddlewareBuilder.buildFreeRoomsPushMiddleware(AlpineBitsVersion.V_2024_10))
                .versionComplete();
    }
}
