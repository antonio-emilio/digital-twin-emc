/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.placeholders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PipelineFunctionBase64DecodeTest {

    private static final PipelineElement KNOWN_INPUT = PipelineElement.resolved("ICAgXyAgIF8gICBfICAgXyAgIF8gIAogIC8gXCAvIFwgLyBcIC8gXCAvIFwgCiAoIEQgfCBJIHwgVCB8IFQgfCBPICkKICBcXy8gXF8vIFxfLyBcXy8gXF8vIA==");
    private static final String ACTUAL_VALUE =
            "   _   _   _   _   _  \n" +
            "  / \\ / \\ / \\ / \\ / \\ \n" +
            " ( D | I | T | T | O )\n" +
            "  \\_/ \\_/ \\_/ \\_/ \\_/ ";

    private final PipelineFunctionBase64Decode function = new PipelineFunctionBase64Decode();

    @Mock
    private ExpressionResolver expressionResolver;

    @After
    public void verifyExpressionResolverUnused() {
        Mockito.verifyNoInteractions(expressionResolver);
    }

    @Test
    public void getName() {
        assertThat(function.getName()).isEqualTo("base64-decode");
    }

    @Test
    public void apply() {
        assertThat(function.apply(KNOWN_INPUT, "()", expressionResolver)).contains(ACTUAL_VALUE);
    }

    @Test
    public void throwsOnNonZeroParameters() {
        assertThatExceptionOfType(PlaceholderFunctionSignatureInvalidException.class)
                .isThrownBy(() -> function.apply(KNOWN_INPUT, "", expressionResolver));
        assertThatExceptionOfType(PlaceholderFunctionSignatureInvalidException.class)
                .isThrownBy(() -> function.apply(KNOWN_INPUT, "(\"string\")", expressionResolver));
        assertThatExceptionOfType(PlaceholderFunctionSignatureInvalidException.class)
                .isThrownBy(() -> function.apply(KNOWN_INPUT, "(\'string\')", expressionResolver));
        assertThatExceptionOfType(PlaceholderFunctionSignatureInvalidException.class)
                .isThrownBy(() -> function.apply(KNOWN_INPUT, "(thing:id)", expressionResolver));
    }

}
