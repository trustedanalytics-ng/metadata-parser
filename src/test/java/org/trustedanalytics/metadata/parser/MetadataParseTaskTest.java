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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.runners.Parameterized.*;

@RunWith(Parameterized.class)
public class MetadataParseTaskTest {

    private final String source;
    private final String targetUri;
    private final String idInObjectStore;
    private final URI defaultFs;
    private final String storeUri;

    public MetadataParseTaskTest(String source, String targetUri, String idInObjectStore, String defaultFs, String storeUri) throws URISyntaxException {
        this.source = source;
        this.targetUri = targetUri;
        this.idInObjectStore = idInObjectStore;
        this.defaultFs = (defaultFs != null) ? new URI(defaultFs) : null;
        this.storeUri = storeUri;
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(new Object[][]{
                // a file from an external source saved inside ObjectStore
                {"https://example.com/example", "hdfs://nameservice1/store/example", "example", "hdfs://nameservice1", "hdfs://nameservice1/store"},
                // store uri provided as a path without scheme and hostname
                {"https://example.com/example", "hdfs://nameservice1/store/example", "example", "hdfs://nameservice1", "/store"},
                // a file kept externally to the ObjectStore
                {"hdfs://nameservice1/out_of_store/test", "hdfs://nameservice1/out_of_store/test", null, null, "hdfs://nameservice1/store"},
        });
    }

    @Test
    public void buildTargetUri() {
        String result = ParseTaskFactory.buildTargetUri(source, storeUri, idInObjectStore, defaultFs);
        assertThat(result, equalTo(targetUri));
    }
}
