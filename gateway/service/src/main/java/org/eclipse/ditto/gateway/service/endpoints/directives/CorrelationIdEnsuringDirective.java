/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.gateway.service.endpoints.directives;

import static org.apache.pekko.http.javadsl.server.Directives.extractRequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.headers.DittoHeaderDefinition;
import org.eclipse.ditto.internal.utils.pekko.logging.DittoLoggerFactory;
import org.eclipse.ditto.internal.utils.pekko.logging.ThreadSafeDittoLogger;

import org.apache.pekko.http.javadsl.model.HttpHeader;
import org.apache.pekko.http.javadsl.model.HttpRequest;
import org.apache.pekko.http.javadsl.server.Route;

/**
 * Custom Pekko Http directive adding a correlationId to the request, if it does not yet exist.
 */
@Immutable
public final class CorrelationIdEnsuringDirective {

    private static final ThreadSafeDittoLogger LOGGER =
            DittoLoggerFactory.getThreadSafeLogger(CorrelationIdEnsuringDirective.class);

    private static final String CORRELATION_ID_HEADER_NAME =
            org.eclipse.ditto.gateway.service.security.HttpHeader.X_CORRELATION_ID.getName();

    private CorrelationIdEnsuringDirective() {
        throw new AssertionError();
    }

    /**
     * Extracts the correlationId from the request or creates a new one, if it does not exist.
     *
     * @param inner the inner Route to provide with the correlationId
     * @return the new Route wrapping {@code inner} with the correlationId
     */
    public static Route ensureCorrelationId(final Function<String, Route> inner) {
        return extractRequestContext(requestContext -> {
            final String correlationId = getCorrelationIdFromHeaders(requestContext.getRequest())
                    .orElseGet(() ->
                            CorrelationIdEnsuringDirective.createNewCorrelationId(requestContext.getRequest()));
            return inner.apply(correlationId);
        });
    }

    private static Optional<String> getCorrelationIdFromHeaders(final HttpRequest request) {
        final Optional<String> result = request.getHeader(CORRELATION_ID_HEADER_NAME)
                .or(() -> request.getHeader(DittoHeaderDefinition.CORRELATION_ID.getKey()))
                .map(HttpHeader::value);

        if (LOGGER.isDebugEnabled()) {
            result.ifPresent(correlationId -> LOGGER.withCorrelationId(headersAsMap(request))
                    .debug("Correlation ID <{}> already exists in request.", correlationId));
        }

        return result;
    }

    private static String createNewCorrelationId(final HttpRequest request) {
        final String result = String.valueOf(UUID.randomUUID());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.withCorrelationId(headersAsMap(request))
                    .debug("Created new correlation ID <{}>.", result);
        }
        return result;
    }

    private static Map<String, String> headersAsMap(final HttpRequest request) {
        return StreamSupport.stream(request.getHeaders().spliterator(), false)
                .collect(Collectors.toMap(HttpHeader::name, HttpHeader::value));
    }

}
