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
package org.trustedanalytics.metadata.security.authorization;

import com.google.common.collect.ImmutableMap;
import org.trustedanalytics.cloud.cc.api.CcOrgPermission;
import org.trustedanalytics.metadata.security.errors.OauthTokenMissingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PlatformAuthorization implements Authorization {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformAuthorization.class);

    private final String userManagementBaseUrl;

    public PlatformAuthorization(String userManagementBaseUrl) {
        this.userManagementBaseUrl = userManagementBaseUrl;
    }

    @Override public boolean checkAccess(HttpServletRequest request, String orgId)
        throws IOException, ServletException {

        LOG.debug(String.format("Check if user can access org: '%s'", orgId));

        String token;
        try {
            token = getToken(request);
        } catch (OauthTokenMissingException e) {
            LOG.debug(e.getMessage(), e);
            return false;
        }

        String url = userManagementBaseUrl + "/rest/orgs/permissions?orgs={org}";
        ResponseEntity<CcOrgPermission[]> access = RestOperationsHelpers.getForEntityWithToken(
            new RestTemplate(), token, url, CcOrgPermission[].class, ImmutableMap.of("org", orgId));
        return access.getBody().length > 0;
    }

    private String getToken(HttpServletRequest request) throws OauthTokenMissingException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            throw new OauthTokenMissingException("Cannot find 'Authorization' header.");
        } else {
            return authHeader.replaceAll("(?i)bearer ", "");
        }
    }
}
