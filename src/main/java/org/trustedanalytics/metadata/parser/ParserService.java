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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import org.trustedanalytics.metadata.exceptions.InvalidUriRuntimeException;
import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class ParserService {

    private static final String CSV = "CSV";
    
    
    // FIXME:If file type is ZIP, we assume that it contains CSV,
    // this whole thing needs some rework, we need to detect type and pass metadata
    // from downloader 
    private static final String ZIPCSV = "ZIP";
    private static final String GZCSV = "GZ";
    
    private static final char RECORD_SEPARATOR = '\n';
    private static final int HEADER_LENGTH = 256;

    public Metadata parse(MetadataParseRequest request, String storeId, InputStream in) {
        Metadata metadata = new Metadata();
        
        metadata.setSourceUri(request.getSource().toString());
        //TODO: why do not use MIME types here, i.e. text/csv ?
        metadata.setFormat(fetchType(metadata.getSourceUri()));
        metadata.setTargetUri(findTargetUri(storeId, request.getIdInObjectStore()));
        metadata.setTitle(request.getTitle());
        metadata.setCategory(request.getCategory());
        metadata.setOrgUUID(request.getOrgUUID());
        metadata.setPublic(request.isPublicRequest());
        
        long size = 0;
        int loaded=0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        char[] buffer = new char[HEADER_LENGTH];
        if (isCsv(metadata)) {
            try {
                //TODO: dataSample should be something more than a header row
                metadata.setDataSample(reader.readLine());
                if(metadata.getDataSample() != null) {
                    size += metadata.getDataSample().length() + 1; // added EOL
                }
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        } else {
            try {
                loaded = reader.read(buffer);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            metadata.setDataSample(new String(buffer, 0, loaded));
            size += loaded;
        }
        
        long recordCount = 0;
        try {
            while ((loaded = reader.read(buffer)) != -1) {
                size += loaded;
                if (isCsv(metadata)) {
                    recordCount += findOccurences(buffer, RECORD_SEPARATOR);
                }
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }

        metadata.setSize(size);
        metadata.setRecordCount(recordCount);
        
        return metadata;
    }
    
    private static long findOccurences(char[] buffer, char c) {
        return CharMatcher
                .is(c)
                .countIn(new String(buffer));
    }

    private static boolean isCsv(Metadata metadata) {
        
        return metadata.getFormat().equals(CSV) ||
               metadata.getFormat().equals(ZIPCSV) ||
               metadata.getFormat().equals(GZCSV);
    }
    
    private static String fetchType(String uriStr) {
        String filename;
        try {
            filename = new URI(uriStr).getPath();
        } catch (URISyntaxException e) {
            throw new InvalidUriRuntimeException(e);
        }

        if(filename.lastIndexOf('.') > filename.lastIndexOf('/'))
        {
            List<String> list = Splitter.on('.').splitToList(filename);
            return list.get(list.size()-1).toUpperCase();
        }
        else
        {
            // if there's no extension just give "CSV"
            return "CSV";
        }
    }

    private static String findTargetUri(String storeId, String idInObjectStore) {
        if(storeId.endsWith("/")) {
            return storeId + idInObjectStore;
        }
        return storeId + "/" + idInObjectStore;
    }
}
