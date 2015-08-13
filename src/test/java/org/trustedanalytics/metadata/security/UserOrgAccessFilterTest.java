/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.metadata.security;

import org.trustedanalytics.metadata.security.authorization.PlatformAuthorization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserOrgAccessFilterTest {

    private static final String TEST_VALID_UUID_STRING = "2a11907a-9086-40c8-bdb7-e96a4c688455";
    private static final UUID TEST_VALID_UUID = UUID.fromString(TEST_VALID_UUID_STRING);
    private static final String TEST_INVALID_UUID_STRING = "invalid uuid";

    private UserOrgAccessFilter sut;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PlatformAuthorization platformAuthorization;

    @Mock
    private BufferedReader requestBodyReader;

    @Before
    public void init() throws IOException {
        sut = new UserOrgAccessFilter(platformAuthorization);
        when(request.getReader()).thenReturn(requestBodyReader);
    }

    private String requestBody(String orgUuid) {
        // @formatter:off
        return
            "{\"id\":\"FAKE\", " +
            "\"source\":\"FAKE\", " +
            "\"idInObjectStore\":\"FAKE\", " +
            "\"callbackUrl\":\"FAKE\", " +
            "\"title\":\"FAKE\", " +
            "\"category\":\"FAKE\", " +
            "\"orgUUID\":\"" + orgUuid + "\"}";
        // @formatter:on
    }

    @Test
    public void testDoFilterInternal_validOrgAndUserHaveAccess()
        throws ServletException, IOException {

        when(requestBodyReader.readLine()).thenReturn(requestBody(TEST_VALID_UUID_STRING))
            .thenReturn(null);
        when(platformAuthorization.checkAccess(request, TEST_VALID_UUID)).thenReturn(true);

        sut.doFilterInternal(request, response, filterChain);

        verify(platformAuthorization).checkAccess(request, TEST_VALID_UUID);
        verify(filterChain).doFilter(any(BufferedRequestWrapper.class), eq(response));
    }

    @Test
    public void testDoFilterInternal_validOrgAndUserDontHaveAccess()
        throws ServletException, IOException {

        when(requestBodyReader.readLine()).thenReturn(requestBody(TEST_VALID_UUID_STRING))
            .thenReturn(null);
        when(platformAuthorization.checkAccess(request, TEST_VALID_UUID)).thenReturn(false);

        sut.doFilterInternal(request, response, filterChain);

        verify(platformAuthorization).checkAccess(request, TEST_VALID_UUID);
        verify(response).sendError(eq(403), any(String.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    public void testDoFilterInternal_invalidOrg() throws ServletException, IOException {

        when(requestBodyReader.readLine()).thenReturn(requestBody(TEST_INVALID_UUID_STRING))
            .thenReturn(null);

        sut.doFilterInternal(request, response, filterChain);

        verify(response).sendError(eq(404), any(String.class));
        verify(filterChain, never()).doFilter(any(), any());
    }
}
