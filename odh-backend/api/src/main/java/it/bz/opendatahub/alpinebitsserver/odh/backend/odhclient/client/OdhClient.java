// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.odh.backend.odhclient.client;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

import java.util.Map;

/**
 * The OdhClient provides methods to fetch data from ODH.
 */
public interface OdhClient {

    /**
     * Fetch data from ODH.
     * <p>
     * The data is read from <code>path</code> using the provided HTTP
     * <code>method</code>. The <code>queryParams</code> are appended
     * to the url and the <code>body</code> is send as the request body.
     * The result is expected of the type defined by <code>genericType</code>.
     *
     * @param path        fetch data from this path
     * @param method      use this HTTP method for the fetch
     * @param queryParams append these parameters to the url
     * @param body        send this body as the request body
     * @param genericType the result is expected to be of this type
     * @param <T>         result type
     * @return data of type T fetched from ODH
     */
    <T> T fetch(String path, String method, Map<String, String> queryParams, Entity<?> body, GenericType<T> genericType);

    /**
     * Fetch data from ODH.
     * <p>
     * The data is read from <code>path</code> using the provided HTTP
     * <code>method</code>. The <code>queryParams</code> are appended
     * to the url and the <code>body</code> is send as the request body.
     * The result is expected to be an instance of <code>resultClass</code>.
     *
     * @param path        fetch data from this path
     * @param method      use this HTTP method for the fetch
     * @param queryParams append these parameters to the url
     * @param body        send this body as the request body
     * @param resultClass the result is expected to be an instance of this class
     * @param <T>         result type
     * @return data of type T fetched from ODH
     */
    <T> T fetch(String path, String method, Map<String, String> queryParams, Entity<?> body, Class<T> resultClass);

}
