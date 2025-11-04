// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2024_10;

import it.bz.opendatahub.alpinebits.middleware.Context;
import it.bz.opendatahub.alpinebits.middleware.Key;
import it.bz.opendatahub.alpinebits.middleware.Middleware;
import it.bz.opendatahub.alpinebits.middleware.MiddlewareChain;

/**
 * This middleware removes elements and attributes that are not allowed in AlpineBits 2024-10
 * Inventory/HotelInfo action.
 * <p>
 * ODH as persisting backend saves AlpineBits inventory data for different AlpineBits versions.
 * Those versions may allow elements and attributes that are not allowed in AlpineBits 2024-10.
 * Therefor it is necessary to adapt the response data for AlpineBits 2024-10 before it is returned.
 * Otherwise, the XML validation may fail.
 * <p>
 * An example for the problem described is the definition of the <code>ContactInfos</code> element,
 * that changed between AlpineBits 2017-10 and 2020-10 versions: in 2017-10, the <code>ContactInfos</code>
 * element was allowed to contain all elements and attributes defined in OTA-2015A. In AlpineBits 2020-10
 * the usage was restricted to a subset of the allowed elements.
 * <p>
 * Please note, that this middleware only removes data to establish compatibility with AlpineBits 2024-10.
 * It does not provide missing data, because that is not possible most of the time. For example, if some
 * required element or attribute is missing (because that is allowed in a different AlpineBits version),
 * there is no way to build the missing data.
 */
public final class InventoryHotelInfoPullAdapter implements Middleware {

    private final it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2020_10.InventoryHotelInfoPullAdapter adapterDelegate;

    public InventoryHotelInfoPullAdapter(Key<String> withExtendedHotelInfoServiceCodesKey) {
        this.adapterDelegate =
                new it.bz.opendatahub.alpinebitsserver.odh.inventory.v_2020_10.InventoryHotelInfoPullAdapter(withExtendedHotelInfoServiceCodesKey);
    }

    @Override
    public void handleContext(Context ctx, MiddlewareChain chain) {
        // Delegate work to AlpineBits 2020-10 InventoryHotelInfoPullAdapter
        // because there is no change for this feature between 2020-10 and 2024-10
        this.adapterDelegate.handleContext(ctx, chain);
    }

}
