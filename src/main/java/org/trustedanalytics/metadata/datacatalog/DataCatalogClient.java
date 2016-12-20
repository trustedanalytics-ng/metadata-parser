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
package org.trustedanalytics.metadata.datacatalog;

import org.trustedanalytics.metadata.parser.api.Metadata;
import org.springframework.web.client.RestOperations;

public class DataCatalogClient implements DataCatalog {

    private final RestOperations restTemplate;
    private final String endpointUrl;

    public DataCatalogClient(RestOperations restTemplate, String endpointUrl) {
        this.restTemplate = restTemplate;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void putMetadata(String orgID, String id, Metadata metadata) {
        metadata.setOrgID(orgID);
        restTemplate.put(endpointUrl+"/rest/datasets/{id}", metadata, id);
    }
}
