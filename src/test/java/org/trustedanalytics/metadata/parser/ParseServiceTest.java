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

import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ParseServiceTest {

    private static final UUID TEST_ORG_UUID = UUID.fromString("09b11c7b-47f7-464f-b146-93b286bce677");

    @Parameters(name = "{index}: getMetadata({0})=({1},{2})")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"https://data.consumerfinance.gov/api/views/x94z-ydhh/rows.csv?accessType=DOWNLOAD", 1, "CSV", "header"},
                {"http://test.com/test.XML", 0, "XML", "header\nrow1\nrow2"},
                {"https://data.consumerfinance.gov/api/views/x94z-ydhh/rows?accessType=DOWNLOAD", 1, "CSV", "header"},
                {"file (1).csv", 1, "CSV", "header"}
        });
    }

    private final String sourceUri;
    private final long recordCount;
    private final String type;
    private final String content;
    private final String targetUri;
    private final String header;
    private final int size;
    
    public ParseServiceTest(String sourceUri, long recordCount, String type, String header) {
        this.sourceUri = sourceUri;
        this.recordCount = recordCount;
        this.type = type;
        this.content = "header\nrow1\nrow2";
        this.targetUri = "teststore/inobjectstore";
        this.header = header;
        this.size = content.length();
    }

    @Test
    public void getMetadata() throws URISyntaxException, IOException {
        MetadataParseRequest request = new MetadataParseRequest();
        request.setIdInObjectStore("inobjectstore");
        request.setSource(sourceUri);
        request.setOrgUUID(TEST_ORG_UUID);

        Metadata metadata = new ParserService().parse(request, "teststore/", new ByteArrayInputStream(content.getBytes()));

        assertThat(metadata, equalTo(metadata()));
    }

    @Test
    public void getMetadata_noSlash() throws URISyntaxException, IOException {
        MetadataParseRequest request = new MetadataParseRequest();
        request.setIdInObjectStore("inobjectstore");
        request.setSource(sourceUri);
        request.setOrgUUID(TEST_ORG_UUID);

        Metadata metadata = new ParserService().parse(request, "teststore", new ByteArrayInputStream(content.getBytes()));

        assertThat(metadata, equalTo(metadata()));
    }

    private Metadata metadata() {
        Metadata metadata = new Metadata();
        metadata.setSourceUri(sourceUri);
        metadata.setTargetUri(targetUri);
        metadata.setRecordCount(recordCount);
        metadata.setFormat(type);
        metadata.setDataSample(header);
        metadata.setSize(size);
        metadata.setOrgUUID(TEST_ORG_UUID);
        
        return metadata;
    }
}
