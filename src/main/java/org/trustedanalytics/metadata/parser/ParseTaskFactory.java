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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.trustedanalytics.metadata.filesystem.FileSystemFactory;
import org.trustedanalytics.metadata.filesystem.HdfsConfigProvider;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.trustedanalytics.store.ObjectStore;
import org.trustedanalytics.store.ObjectStoreFactory;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import javax.security.auth.login.LoginException;

@Service
public class ParseTaskFactory {

    private static final String HDFS_FULL_PATH_INDICATOR = "hdfs://";
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseTaskFactory.class);

    private final ObjectStoreFactory<UUID> objectStoreFactory;
    private final ParserService parserService;
    private final FileSystemFactory fileSystemFactory;
    private final HdfsConfigProvider hdfsConfigProvider;

    @Autowired
    public ParseTaskFactory(ObjectStoreFactory<UUID> objectStoreFactory, ParserService parserService, FileSystemFactory fileSystemFactory, HdfsConfigProvider hdfsConfigProvider) {
        this.objectStoreFactory = objectStoreFactory;
        this.parserService = parserService;
        this.fileSystemFactory = fileSystemFactory;
        this.hdfsConfigProvider = hdfsConfigProvider;
    }

    public MetadataParseTask newParseTask(MetadataParseRequest request, DataCatalog dataCatalog,
        RestOperations restOperations) throws HdfsRequestException, IOException, LoginException, InterruptedException {

        ObjectStore objectStore = objectStoreFactory.create(request.getOrgUUID());

        String targetUri = buildTargetUri(request.getSource(), objectStore.getId(),
                request.getIdInObjectStore(), hdfsConfigProvider.getDefaultFs());

        LOGGER.info("Creating task for request: " + request.toString());

        return new MetadataParseTask(dataCatalog,
                                     request,
                                     restOperations,
                                     parserService,
                                     fileSystemFactory.getFileSystem(request.getOrgUUID()),
                                     new Path(targetUri)
        );
    }

    static String buildTargetUri(String source, String objectStoreId, String objectId, URI defaultUri) {
        // if it's full hdfs path, then source is the same as target
        if (source.startsWith(HDFS_FULL_PATH_INDICATOR)) {
            return source;
        }

        String path = objectStoreId;

        // prepend path with defaultUri, if it's just a path without scheme spec
        if (!objectStoreId.startsWith(HDFS_FULL_PATH_INDICATOR)) {
            path = defaultUri + path;
        }

        // ensure path ends with '/'
        if (!objectStoreId.endsWith("/")) {
            path += "/";
        }

        return path + objectId;
    }

}
