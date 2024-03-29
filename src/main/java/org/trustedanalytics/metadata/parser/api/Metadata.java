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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

import lombok.Data;

/**
 * Metadata representation. This object is passed to Data Catalog. It needs to
 * be serialized in a strict way to be readable by the Data Catalog. By default
 * we are dealing with empty file and therefore data sample is empty string and
 * record count is set to 0.
 */
@Data
public class Metadata {

    private String dataSample;
    private long size;
    private String sourceUri;
    private String targetUri;
    private String format;
    private long recordCount;
    private String title;    
    private String category;

    @JsonProperty("orgUUID")
    private String orgID;

    @JsonProperty("isPublic")
    private boolean isPublic;

    public Metadata() {

    }

    public Metadata(MetadataParseRequest request)
            throws IOException {
        title = request.getTitle();
        category = request.getCategory();
        orgID = request.getOrgID();
        isPublic = request.isPublicRequest();
        sourceUri = request.getSource();
        dataSample = "";
        recordCount = 0;
    }

    @JsonIgnore
    public boolean getPublic() {
        return isPublic;
    }

    @JsonIgnore
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

}
