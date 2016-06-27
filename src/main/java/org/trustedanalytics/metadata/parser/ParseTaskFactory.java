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

import org.apache.hadoop.fs.Path;
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

    private static final CharSequence HDFS_FULL_PATH_INDICATOR = "hdfs://";
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

        tryToIdentifyIdInObjectStore(request, objectStore.getId());

        LOGGER.info("Creating task for request: " + request.toString());
        return new MetadataParseTask(objectStore,
                                     dataCatalog,
                                     request,
                                     restOperations,
                                     parserService,
                                     fileSystemFactory.getFileSystem(request.getOrgUUID()),
                                     getDatasetPath(request, objectStore.getId())
        );
    }

    public static boolean isSourceFullHdfsPath(MetadataParseRequest request) {
        return request.getSource().contains(HDFS_FULL_PATH_INDICATOR);
    }

    private Path getDatasetPath(MetadataParseRequest request, String objectStoreId) {
        if (!isSourceFullHdfsPath(request)) {
            return createFullHdfsPath(request, objectStoreId);
        }
        return new Path(request.getSource());
    }

    private void tryToIdentifyIdInObjectStore(MetadataParseRequest request, String objectStoreId) {
        if (isSourceFullHdfsPath(request) && request.getSource().contains(objectStoreId)) {
            int objectStorePartIdx = request.getSource().indexOf(objectStoreId);
            request.setIdInObjectStore(request.getSource().substring(objectStorePartIdx + objectStoreId.length()));
            LOGGER.info("Id in object store: {}", request.getIdInObjectStore());
        }
    }

    private Path createFullHdfsPath(MetadataParseRequest request, String objectStoreId) {
        return new Path(objectStoreId + "/" + request.getIdInObjectStore());
    }
    
}
