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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class OrgGuidRetriever {

    private static final Logger LOG = LoggerFactory.getLogger(OrgGuidRetriever.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private OrgGuidRetriever() {
    }

    public static UUID getOrgGuidFromRequestBody(String body) {
        LOG.info("." + body + ".");
        try {
            MetadataParseRequest request =
                mapper.readValue(body, MetadataParseRequest.class);
            return request.getOrgUUID();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot parse request body", e);
        }
    }
}
