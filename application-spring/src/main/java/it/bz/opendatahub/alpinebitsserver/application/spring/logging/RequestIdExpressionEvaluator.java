// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: MPL-2.0

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.bz.opendatahub.alpinebitsserver.application.spring.logging;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

/**
 * A logback logging evaluator that checks if the MDC contains a "requestId" entry and,
 * depending on configuration, evaluates to true if the requestId is present or missing.
 */
public class RequestIdExpressionEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    private boolean onlyWhenRequestIdPresent;
    private boolean onlyWhenRequestIdMissing;

    public void setOnlyWhenRequestIdPresent(boolean onlyWhenRequestIdPresent) {
        this.onlyWhenRequestIdPresent = onlyWhenRequestIdPresent;
    }

    public void setOnlyWhenRequestIdMissing(boolean onlyWhenRequestIdMissing) {
        this.onlyWhenRequestIdMissing = onlyWhenRequestIdMissing;
    }

    @Override
    public void start() {
        if (onlyWhenRequestIdPresent && onlyWhenRequestIdMissing) {
            addError("Invalid config: both onlyWhenRequestIdPresent and onlyWhenRequestIdMissing are true.");
            return;
        }
        super.start();
    }

    @Override
    public boolean evaluate(ILoggingEvent event) {
        if (!isStarted()) {
            return false;
        }

        String requestId = event.getMDCPropertyMap().get("requestId");
        boolean present = requestId != null;

        if (onlyWhenRequestIdPresent) {
            return present;
        }

        if (onlyWhenRequestIdMissing) {
            return !present;
        }

        // If neither flag is set, then default to false
        return false;
    }


}