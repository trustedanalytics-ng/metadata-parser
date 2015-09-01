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

import org.springframework.web.client.RestOperations;
import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.trustedanalytics.metadata.datacatalog.DataCatalogClient;
import org.trustedanalytics.metadata.datacatalog.DataCatalogFactory;

import java.util.Objects;

//we use this to enable setting dataCatalogUrl once application context is ready
//we need this in integration tests, because they're starting data catalog mock on a random port
//and we need a way to pass this port to below factory
public class RuntimeConfigurableDataCatalogFactory extends DataCatalogFactory {

    private String dataCatalogUrl =
        "YOU-NEED-TO-SET-THIS-VARIABLE-ONCE-APPLICATION-CONTEXT-IS-READY";

    public void setDataCatalogUrl(String dataCatalogUrl) {
        this.dataCatalogUrl = dataCatalogUrl;
    }

    @Override
    public DataCatalog get(RestOperations restOperations) {
        Objects.requireNonNull(restOperations);

        return new DataCatalogClient(restOperations, dataCatalogUrl);
    }
}
