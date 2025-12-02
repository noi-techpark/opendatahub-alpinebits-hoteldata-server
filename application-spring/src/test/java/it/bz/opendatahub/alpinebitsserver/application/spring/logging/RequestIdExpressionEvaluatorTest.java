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
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link RequestIdExpressionEvaluator}.
 */
public class RequestIdExpressionEvaluatorTest {

    @Test
    void start_shouldLogError_WhenBothConfigParamsAreTrue() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdPresent(true);
        evaluator.setOnlyWhenRequestIdMissing(true);

        // should not throw and should not be started when both flags are true
        evaluator.start();
        assertFalse(evaluator.isStarted());

        // event with requestId should still evaluate to false because evaluator is not started
        ILoggingEvent eventWithRequestId = Mockito.mock(ILoggingEvent.class);
        Mockito.when(eventWithRequestId.getMDCPropertyMap()).thenReturn(
                Map.of("requestId", "id-123")
        );

        assertFalse(evaluator.evaluate(eventWithRequestId));
    }

    @Test
    void evaluate_returnsTrue_whenRequestIdPresentFlagIsSetAndRequestIdExists() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdPresent(true);
        evaluator.start();

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of("requestId", "id-123"));

        assertTrue(evaluator.evaluate(event));
    }

    @Test
    void evaluate_returnsFalse_whenRequestIdPresentFlagIsSetAndRequestIdMissing() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdPresent(true);
        evaluator.start();

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of());

        assertFalse(evaluator.evaluate(event));
    }

    @Test
    void evaluate_returnsTrue_whenRequestIdMissingFlagIsSetAndRequestIdMissing() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdMissing(true);
        evaluator.start();

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of());

        assertTrue(evaluator.evaluate(event));
    }

    @Test
    void evaluate_returnsFalse_whenRequestIdMissingFlagIsSetAndRequestIdExists() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdMissing(true);
        evaluator.start();

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of("requestId", "id-123"));

        assertFalse(evaluator.evaluate(event));
    }

    @Test
    void evaluate_returnsFalse_whenNoFlagsAreSet() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.start();

        ILoggingEvent eventWithRequestId = Mockito.mock(ILoggingEvent.class);
        Mockito.when(eventWithRequestId.getMDCPropertyMap()).thenReturn(Map.of("requestId", "id-123"));

        ILoggingEvent eventWithoutRequestId = Mockito.mock(ILoggingEvent.class);
        Mockito.when(eventWithoutRequestId.getMDCPropertyMap()).thenReturn(Map.of());

        assertFalse(evaluator.evaluate(eventWithRequestId));
        assertFalse(evaluator.evaluate(eventWithoutRequestId));
    }

    @Test
    void evaluate_returnsFalse_whenEvaluatorNotStarted() {
        RequestIdExpressionEvaluator evaluator = new RequestIdExpressionEvaluator();
        evaluator.setOnlyWhenRequestIdPresent(true);

        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of("requestId", "id-123"));

        assertFalse(evaluator.evaluate(event));
    }

}