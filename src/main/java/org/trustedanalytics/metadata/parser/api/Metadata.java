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
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Metadata representation.
 * This object is passed to Data Catalog.
 * It needs to be serialized in a strict way to be readable by the Data Catalog.
 */
public class Metadata {

    @Getter @Setter private String dataSample;
    @Getter @Setter private long size;
    @Getter @Setter private String sourceUri;
    @Getter @Setter private String targetUri;
    @Getter @Setter private String format;
    @Getter @Setter private long recordCount;
    @Getter @Setter private String title;
    @Getter @Setter private String category;
    @Getter @Setter private UUID orgUUID;
    @JsonProperty("isPublic") private boolean isPublic;

    @JsonIgnore
    public boolean getPublic() {
        return isPublic;
    }

    @JsonIgnore
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
            .add("dataSample", dataSample)
            .add("size", size)
            .add("sourceUri", sourceUri)
            .add("targetUri", targetUri)
            .add("format", format)
            .add("recordCount", recordCount)
            .add("title", title)
            .add("category", category)
            .add("orgUUID", orgUUID)
            .add("isPublic", isPublic)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSample, size, sourceUri, targetUri, format, recordCount, title, category, orgUUID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Metadata other = (Metadata) obj;
        return Objects.equals(this.dataSample, other.dataSample)
            && Objects.equals(this.size, other.size)
            && Objects.equals(this.sourceUri, other.sourceUri)
            && Objects.equals(this.targetUri, other.targetUri)
            && Objects.equals(this.format, other.format)
            && Objects.equals(this.recordCount, other.recordCount)
            && Objects.equals(this.title, other.title)
            && Objects.equals(this.category, other.category)
            && Objects.equals(this.orgUUID, other.orgUUID)
            && Objects.equals(this.isPublic, other.isPublic);
    }
}
