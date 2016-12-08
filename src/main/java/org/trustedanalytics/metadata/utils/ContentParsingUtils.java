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
package org.trustedanalytics.metadata.utils;

import org.trustedanalytics.metadata.exceptions.InputStreamParseException;
import org.trustedanalytics.metadata.parser.api.Metadata;

import com.google.common.base.Throwables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ContentParsingUtils {

    public static final int HEADER_LENGTH = 256;
    private static final char RECORD_SEPARATOR = '\n';
    private static final int BUFFER_SIZE = 1000000;

    private ContentParsingUtils() {
    }

    public static Metadata parseCsv(Metadata metadata, InputStream in)
            throws IOException {
        long size = 0;
        long recordCount = 0;
        char[] buffer = new char[BUFFER_SIZE];
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
            final String headerRow = reader.readLine();
            if (headerRow != null) {
                metadata.setDataSample(headerRow);
                size += metadata.getDataSample().length();
                recordCount += 1;
                int loaded;
                while ((loaded = reader.read(buffer)) != -1) {
                    size += loaded;
                    recordCount += findOccurrences(buffer, RECORD_SEPARATOR, loaded);
                }

                if (size > headerRow.length()) {
                    size++; // include newline between header row and rest of the rows
                }
            }
        } catch (IOException|InputStreamParseException e) {
            Throwables.propagate(e);
        }

        metadata.setSize(size);
        metadata.setRecordCount(recordCount);

        return metadata;
    }

    public static Metadata parseGenericFile(Metadata metadata, InputStream in)
            throws IOException {
        long size = 0;
        long recordCount = 0;

        char[] header = new char[HEADER_LENGTH];
        char[] buffer = new char[BUFFER_SIZE];
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            int loaded = reader.read(header);
            // is file not empty
            if(loaded > 0) {
                metadata.setDataSample(new String(header, 0, loaded));
                size += loaded;
                while ((loaded = reader.read(buffer)) != -1) {
                    size += loaded;
                }
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }

        metadata.setSize(size);
        metadata.setRecordCount(recordCount);
        return metadata;
    }

    public static long findOccurrences(char[] buffer, char c, int lenght) {
        long counter = 0;
        for (int i = 0; i < lenght; ++i) {
            if (buffer[i] == c) {
                counter++;
            }
        }
        return counter;
    }

}
