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

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.trustedanalytics.metadata.parser.api.MetadataParseStatus;
import org.trustedanalytics.store.ObjectStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.*;

public class MetadataParseTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataParseTask.class);

    private final MetadataParseRequest request;
    private final DataCatalog dataCatalog;
    private final RestOperations restTemplate;
    private final ParserService parserService;
    private final FileSystem fileSystem;
    private final Path hdfsPath;


    public MetadataParseTask(DataCatalog dataCatalog, MetadataParseRequest metadataDescriptor, RestOperations restTemplate, ParserService parserService, FileSystem fileSystem, Path hdfsPath) {
        this.request = metadataDescriptor;
        this.dataCatalog = dataCatalog;
        this.restTemplate = restTemplate;
        this.parserService = parserService;
        this.fileSystem = fileSystem;
        this.hdfsPath = hdfsPath;
    }

    @Override
    public void run() {
        try (SequenceInputStream in = new SequenceInputStream(getInputStreamEnumeration(hdfsPath))) {
            Metadata metadata = parserService.parse(request, in);
            metadata.setTargetUri(hdfsPath.toString());
            if (isDirectory(hdfsPath)) {
                makeSureTargetPathEndsWithSlash(metadata);
            }

            dataCatalog.putMetadata(request.getOrgID(), request.getId(), metadata);
            notifyDone();
        } catch (Exception e) {
            notifyFailed(String.format("Cannot parse resource  %s . %s", hdfsPath, e.getMessage()), e);
        }
    }

    private void makeSureTargetPathEndsWithSlash(Metadata metadata) {
        if (!metadata.getTargetUri().endsWith("/")) {
            metadata.setTargetUri(metadata.getTargetUri() + "/");
        }
    }

    private Enumeration<InputStream> getInputStreamEnumeration(Path sourcePath) throws IOException {
        List<InputStream> inputStreams = new ArrayList<>();

        if (isDirectory(sourcePath)) {
            LOG.info("Directory recognized, searching for files");
            processDirectory(sourcePath, inputStreams);
        } else {
            processFile(sourcePath, inputStreams);
        }
        return Collections.enumeration(inputStreams);
    }

    private boolean isDirectory(Path sourcePath) throws IOException {
        return fileSystem.isDirectory(sourcePath);
    }

    private void processFile(Path sourcePath, List<InputStream> inputStreams) throws IOException {
        inputStreams.add(fileSystem.open(sourcePath));
    }

    private void processDirectory(Path sourcePath, List<InputStream> inputStreams) throws IOException {
        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(sourcePath, false);
        while (iterator.hasNext()) {
            LocatedFileStatus status = iterator.next();
            if (status.isFile()) {
                LOG.info("File found: {}", status.getPath());
                processFile(status.getPath(), inputStreams);
            }
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
