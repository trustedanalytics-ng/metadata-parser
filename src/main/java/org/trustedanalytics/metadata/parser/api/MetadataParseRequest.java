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
package org.trustedanalytics.metadata.parser.api;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

@Data
public class MetadataParseRequest {

    private String id;
    private String  source;
    private String idInObjectStore;
    private URI callbackUrl;
    private String title;
    private String category;
    private UUID orgUUID;
    private boolean publicRequest;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataParseRequest.class);



    @Override
    public String toString() {
        return "MetadataParseRequest [id=" + id + ", source=" + source + ", idInObjectStore="
                + idInObjectStore + ", callbackUrl=" + callbackUrl + ", title=" + title
                + ", category=" + category + ", orgUUID=" + orgUUID + ", publicRequest=" + publicRequest + "]";
    }


}
