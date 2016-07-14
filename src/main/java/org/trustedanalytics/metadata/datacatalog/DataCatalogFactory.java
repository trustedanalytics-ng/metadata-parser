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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.Objects;

@Service
@Profile({"cloud", "default", "kube"})
public class DataCatalogFactory {

  @Value("${dependencies.datacatalog}")
  private String dataCatalogUrl;

  public DataCatalog get(RestOperations restOperations) {
    Objects.requireNonNull(restOperations);

    return new DataCatalogClient(restOperations, dataCatalogUrl);
  }

}
