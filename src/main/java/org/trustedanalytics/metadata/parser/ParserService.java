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
import org.trustedanalytics.metadata.utils.ContentParsingUtils;
import org.trustedanalytics.metadata.utils.MediaType;
import org.trustedanalytics.metadata.utils.ContentDetectionUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ParserService {

    public static final char RECORD_SEPARATOR = '\n';
    public static final int HEADER_LENGTH = 256;
    public static final int BUFFER_SIZE = 1000000;

    public Metadata parse(MetadataParseRequest request, String storeId,
            InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);

        Metadata metadata = new Metadata(request, storeId);
        
        metadata.setFormat(ContentDetectionUtils.bestGuessFileType(bin, metadata.getSourceUri()));

        if (isCsv(metadata)) {
            metadata = ContentParsingUtils.parseCsv(metadata, bin);
        } else {
            metadata = ContentParsingUtils.parseGenericFile(metadata, bin);
        }

        return metadata;
    }

    private static boolean isCsv(Metadata metadata) {
        return MediaType.CSV.is(metadata.getFormat());
    }
  
}
