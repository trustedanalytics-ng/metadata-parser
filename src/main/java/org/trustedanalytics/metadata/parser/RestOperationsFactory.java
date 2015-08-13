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
package org.trustedanalytics.metadata.parser;

import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.cloud.auth.HeaderAddingHttpInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Factory instantiate and configure RestOperations according to authentication method that should
 * be used in any future interactions with remote hosts.
 */

@Service
public class RestOperationsFactory {

  private final AuthTokenRetriever tokenRetriever;

  @Autowired
  public RestOperationsFactory(AuthTokenRetriever tokenRetriever) {
    this.tokenRetriever = tokenRetriever;
  }

  public RestOperations oAuth(Authentication authentication) {
    final String token = tokenRetriever.getAuthToken(authentication);
    final RestTemplate restTemplate = new RestTemplate();

    restTemplate.setInterceptors(Collections.singletonList(new HeaderAddingHttpInterceptor(
        HttpHeaders.AUTHORIZATION, "bearer " + token)));

    return restTemplate;
  }

}
