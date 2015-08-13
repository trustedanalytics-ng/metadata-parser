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

package org.trustedanalytics.metadata.datacatalog;

import org.trustedanalytics.metadata.parser.api.Metadata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class DataCatalogClientTest {
    /**
     * This test should prevent anyone from carelessly breaking the service communication.
     * */
    @Test
    public void metadataSerialization_requestProperlySerialized() throws IOException {
        // RestTemplate is using Jackson so we use it in the test
        ObjectMapper mapper = new ObjectMapper();

        Map<String, String> properSerilizedValues = new HashMap<String, String>() {{
            put("category", "test category");
            put("dataSample", "test sample");
            put("format", "test format");
            put("isPublic", "true");
            put("orgUUID", UUID.randomUUID().toString());
            put("recordCount", "13");
            put("size", "123");
            put("sourceUri", "test source uri");
            put("targetUri", "test target uri");
            put("title", "test title");
        }};

        Metadata meta = new Metadata();

        meta.setCategory(properSerilizedValues.get("category"));
        meta.setDataSample(properSerilizedValues.get("dataSample"));
        meta.setFormat(properSerilizedValues.get("format"));
        meta.setPublic(Boolean.valueOf(properSerilizedValues.get("isPublic")));
        meta.setOrgUUID(UUID.fromString(properSerilizedValues.get("orgUUID")));
        meta.setRecordCount(Integer.valueOf(properSerilizedValues.get("recordCount")));
        meta.setSize(Integer.valueOf(properSerilizedValues.get("size")));
        meta.setSourceUri(properSerilizedValues.get("sourceUri"));
        meta.setTargetUri(properSerilizedValues.get("targetUri"));
        meta.setTitle(properSerilizedValues.get("title"));

        String serializedMeta = mapper.writeValueAsString(meta);
        Map<String, String> deserializedMap = mapper.readValue(
            serializedMeta,
            new TypeReference<HashMap<String, String>>(){});

        Assert.assertEquals(properSerilizedValues, deserializedMap);
    }
}
