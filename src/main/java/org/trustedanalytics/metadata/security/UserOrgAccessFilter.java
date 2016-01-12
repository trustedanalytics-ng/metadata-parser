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

import org.trustedanalytics.metadata.security.authorization.Authorization;
import org.trustedanalytics.metadata.security.authorization.OrgGuidRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class UserOrgAccessFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(UserOrgAccessFilter.class);

    private Authorization authorization;

    @Autowired
    public UserOrgAccessFilter(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override protected void doFilterInternal(HttpServletRequest innerRequest,
        HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {

        BufferedRequestWrapper request = new BufferedRequestWrapper(innerRequest);

        UUID orgId;
        try {
            orgId = OrgGuidRetriever.getOrgGuidFromRequestBody(request.getBody());
            LOG.debug(String.format("Org user want to access: '%s'", orgId));
        } catch (IllegalArgumentException e) {
            LOG.debug(e.getMessage(), e);
            httpServletResponse.sendError(404, "Org GUID could not be retreived from request body.");
            return;
        }

        if (authorization.checkAccess(innerRequest, orgId)) {
            LOG.debug("User access confirmed.");
            filterChain.doFilter(request, httpServletResponse);
        } else {
            LOG.debug("User access denied.");
            httpServletResponse.sendError(403, "You can't access specified organization.");
        }
    }
}
