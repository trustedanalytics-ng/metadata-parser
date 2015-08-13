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

import java.io.InputStream;

import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.trustedanalytics.metadata.parser.api.MetadataParseStatus;
import org.trustedanalytics.store.ObjectStore;

//FIXME: There's something wrong about this class - to many dependencies, to little logic
public class MetadataParseTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataParseTask.class);
    
    private final ObjectStore objectStore;
    private final MetadataParseRequest request;
    private final DataCatalog dataCatalog;
    private final RestOperations restTemplate;
    private final ParserService parserService;
    
    public MetadataParseTask(ObjectStore objectStore, DataCatalog dataCatalog, MetadataParseRequest metadataDescriptor, RestOperations restTemplate, ParserService parserService) {
        this.objectStore = objectStore;
        this.request = metadataDescriptor;
        this.dataCatalog = dataCatalog;
        this.restTemplate = restTemplate;
        this.parserService = parserService;
    }

    @Override
    public void run() {
        try (InputStream in = objectStore.getContent(request.getIdInObjectStore())) {
            Metadata metadata = parserService.parse(request, objectStore.getId(), in);
            dataCatalog.putMetadata(request.getOrgUUID(), request.getId(), metadata);
            notifyDone();
        } catch (Exception e) {
            notifyFailed("Cannot parse the file "+request.getIdInObjectStore()+". "+e.getMessage(), e);
        }
    }

    private void notifyDone() {
        restTemplate.postForEntity(request.getCallbackUrl(), MetadataParseStatus.done(), String.class);
    }
    
    private void notifyFailed(String msg, Exception e) {
        LOG.error(msg, e);
        restTemplate.postForEntity(request.getCallbackUrl(), MetadataParseStatus.failed(), String.class);
    }
}
