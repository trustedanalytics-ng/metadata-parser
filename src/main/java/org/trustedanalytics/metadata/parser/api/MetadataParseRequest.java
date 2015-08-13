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

import java.net.URI;
import java.util.UUID;

public class MetadataParseRequest {

    private String id;
    private URI source;
    private String idInObjectStore;
    private URI callbackUrl;
    private String title;
    private String category;
    private UUID orgUUID;
    private boolean publicRequest;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public URI getSource() {
        return source;
    }

    public void setSource(URI source) {
        this.source = source;
    }

    public String getIdInObjectStore() {
        return idInObjectStore;
    }

    public void setIdInObjectStore(String idInObjectStore) {
        this.idInObjectStore = idInObjectStore;
    }

    public URI getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(URI callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UUID getOrgUUID() {
        return orgUUID;
    }

    public void setOrgUUID(UUID orgUUID) {
        this.orgUUID = orgUUID;
    }

    public boolean isPublicRequest() {
        return publicRequest;
    }

    public void setPublicRequest(boolean publicRequest) {
        this.publicRequest = publicRequest;
    }

    @Override
    public String toString() {
        return "MetadataParseRequest [id=" + id + ", source=" + source + ", idInObjectStore="
                + idInObjectStore + ", callbackUrl=" + callbackUrl + ", title=" + title
                + ", category=" + category + ", orgUUID=" + orgUUID + ", publicRequest=" + publicRequest + "]";
    }
}
