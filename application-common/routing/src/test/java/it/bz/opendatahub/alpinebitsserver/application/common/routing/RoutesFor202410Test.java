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
import it.bz.opendatahub.alpinebits.routing.DefaultRouter;
import it.bz.opendatahub.alpinebits.routing.Router;
import it.bz.opendatahub.alpinebits.routing.RoutingBuilder;
import it.bz.opendatahub.alpinebits.routing.constants.Action;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link RoutesFor202410}.
 */
public class RoutesFor202410Test {

    private static final String ALPINEBITS_VERSION = AlpineBitsVersion.V_2024_10;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRoutes_ShouldThrow_WhenBuilderIsNull() {
        RoutesFor202410.routes(null);
    }

    @Test
    public void testRoutes_ShouldReturnFinalBuild_OnSuccess() {
        RoutingBuilder.FinalBuilder builder = RoutesFor202410.routes(new DefaultRouter.Builder());
        Router router = builder.buildRouter();

        assertEquals(router.getVersions().size(), 1);
        assertTrue(router.getVersions().contains(ALPINEBITS_VERSION));

        assertTrue(router.getActionsForVersion(ALPINEBITS_VERSION).isPresent());
        Set<Action> actions = router.getActionsForVersion(ALPINEBITS_VERSION).get();

        assertEquals(actions.size(), 6);
        assertTrue(actions.contains(Action.HANDSHAKING));
        assertTrue(actions.contains(Action.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_FREE_ROOMS));

        assertTrue(router.getCapabilitiesForVersion(ALPINEBITS_VERSION).isPresent());
        Set<String> capabilities = router.getCapabilitiesForVersion(ALPINEBITS_VERSION).get();

        assertEquals(capabilities.size(), 14);
        assertTrue(capabilities.contains(AlpineBitsCapability.HANDSHAKING));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_CATEGORIES));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_ROOMS));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_OUT_OF_ORDER));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_OUT_OF_MARKET));
        assertTrue(capabilities.contains(AlpineBitsCapability.FREE_ROOMS_HOTEL_INV_COUNT_NOTIF_ACCEPT_CLOSING_SEASONS));
    }


}