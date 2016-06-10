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

import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.trustedanalytics.metadata.filesystem.FileSystemFactory;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.trustedanalytics.store.ObjectStore;
import org.trustedanalytics.store.ObjectStoreFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.UUID;

@Service
public class ParseTaskFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseTaskFactory.class);

    private final ObjectStoreFactory<UUID> objectStoreFactory;
    private final ParserService parserService;
    private FileSystemFactory fileSystemFactory;

    @Autowired
    public ParseTaskFactory(ObjectStoreFactory<UUID> objectStoreFactory, ParserService parserService, FileSystemFactory fileSystemFactory) {
        this.objectStoreFactory = objectStoreFactory;
        this.parserService = parserService;
        this.fileSystemFactory = fileSystemFactory;
    }

    public MetadataParseTask newParseTask(MetadataParseRequest request, DataCatalog dataCatalog,
        RestOperations restOperations) throws HdfsRequestException, IOException, LoginException, InterruptedException {

        ObjectStore objectStore = objectStoreFactory.create(request.getOrgUUID());

        request.tryToIdentifyIdInObjectStore(objectStore.getId());
        
        request.createFullHdfsPathIfNotPresent(objectStore.getId());

        LOGGER.info("Creating task for request: " + request.toString());
        return new MetadataParseTask(objectStore,
                                     dataCatalog,
                                     request,
                                     restOperations,
                                     parserService,
                                     fileSystemFactory.getFileSystem(request.getOrgUUID()));
    }

}
