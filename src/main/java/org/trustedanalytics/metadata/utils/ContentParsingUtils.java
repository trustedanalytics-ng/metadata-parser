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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.trustedanalytics.metadata.parser.ParserService;
import org.trustedanalytics.metadata.parser.api.Metadata;

import com.google.common.base.Throwables;

public class ContentParsingUtils {

    public static Metadata parseCsv(Metadata metadata, InputStream in)
            throws IOException {
        long size = 0;
        int loaded = 0;
        long recordCount = 0;
        char[] buffer = new char[ParserService.BUFFER_SIZE];
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
            String headerRow = reader.readLine();
            String secondRow = reader.readLine();

            String sample = headerRow;
            if (secondRow != null) {
                sample += "\n" + secondRow + "\n";
                recordCount++;
            }

            metadata.setDataSample(sample);
            if (metadata.getDataSample() != null) {
                size += metadata.getDataSample().length();
            }
            while ((loaded = reader.read(buffer)) != -1) {
                size += loaded;
                recordCount += ContentParsingUtils.findOccurrences(buffer,
                        ParserService.RECORD_SEPARATOR, loaded);
            }
        } catch (IOException e) {
            Throwables.propagate(e);
        }

        metadata.setSize(size);
        metadata.setRecordCount(recordCount);

        return metadata;
    }

    public static Metadata parseGenericFile(Metadata metadata, InputStream in)
            throws IOException {
        long size = 0;
        int loaded = 0;
        long recordCount = 0;

        char[] header = new char[ParserService.HEADER_LENGTH];
        char[] buffer = new char[ParserService.BUFFER_SIZE];
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            loaded = reader.read(header);
            metadata.setDataSample(new String(header, 0, loaded));
            size += loaded;
            while ((loaded = reader.read(buffer)) != -1) {
                size += loaded;
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
