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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@RunWith(Parameterized.class)
public class ParseServiceTest {

    private static final UUID TEST_ORG_UUID = UUID.fromString("09b11c7b-47f7-464f-b146-93b286bce677");
    
    private static String readFile(String fileName, String encoding) throws IOException {        
        InputStream in = ParseServiceTest.class.getClassLoader().getResourceAsStream(fileName);
        return IOUtils.toString(in, "UTF-8");
    }
    
    private static String readFileLines(String fileName, String encoding, int lines) throws IOException {
        
        InputStream in = ParseServiceTest.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,encoding));
        
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i < lines; ++i) {
            builder.append(reader.readLine());
            builder.append("\n");
        }
        in.close();
        reader.close();
        return builder.toString();
    }
    
    private static String readFileBytes(String fileName, String encoding, int bytes) throws IOException {
        InputStream in = ParseServiceTest.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,encoding));
        char [] buffer = new char[bytes];
        reader.read(buffer);
        reader.close();
        in.close();
        return String.valueOf(buffer);
    }

    @Parameters(name = "{index}: getMetadata({0})=({1},{2},{3})")
    public static Collection<Object[]> data() throws IOException {
        return Arrays.asList(new Object[][]{ 
                {"file (1).csv", 1, "CSV", "header", "header"},
                {"https://data.consumerfinance.gov/api/views/x94z-ydhh/rows.csv?accessType=DOWNLOAD", 3, "CSV","header\nrow1\nrow2\n", "header"},
                {"http://test.com/compressed.tar.gz", 0, "XML", "<?xml version=\"1.0\" encoding=\"UTF-8\">\n<tag>\n</tag>", "<?xml version=\"1.0\" encoding=\"UTF-8\">\n<tag>\n</tag>"},
                {"http://test.com/test.CSV", 3, "CSV", "header1,header2,header3\nval1,val2,val3\nval4,val5,val6\n","header1,header2,header3"},
                {"https://data.consumerfinance.gov/api/views/x94z-ydhh/rows?accessType=DOWNLOAD", 3, "CSV", "header1,header2,header3\nval1,val2,val3\nval4,val5,val6\n", "header1,header2,header3"},
                {"https://data.consumerfinance.gov/index.html", 0, "HTML", "<HTML><HEAD><HEAD><BODY></BODY></HTML>","<HTML><HEAD><HEAD><BODY></BODY></HTML>"},
                {"https://data.consumerfinance.gov", 0, "HTML", "<HTML><HEAD><HEAD><BODY></BODY></HTML>","<HTML><HEAD><HEAD><BODY></BODY></HTML>"},
                {"https://data.consumerfinance.gov", 0, "JSON", "{ \"record\" : { \"val\":1, \"name\": \"John Doe\"}}","{ \"record\" : { \"val\":1, \"name\": \"John Doe\"}}"},
                {"https://data.consumerfinance.gov", 0, "JSON", readFile("sample.json", "UTF-8"), readFile("sample.json", "UTF-8")},
                {"https://data.consumerfinance.gov", 0, "XML", readFile("sample.xml", "UTF-8"), readFile("sample.xml", "UTF-8")},
                {"https://data.consumerfinance.gov", 5, "CSV", readFile("sample.csv", "UTF-8"), readFileLines("sample.csv", "UTF-8", 1).trim()},
                {"https://data.consumerfinance.gov", 0, "JSON", readFile("sample2.json", "UTF-8"), readFileBytes("sample2.json", "UTF-8", ParserService.HEADER_LENGTH)},
                {"https://data.consumerfinance.gov", 0, "JSON", readFile("large.json", "UTF-8"), readFileBytes("large.json", "UTF-8",ParserService.HEADER_LENGTH)},
                {"https://data.consumerfinance.gov", 1001, "CSV", readFile("large.csv", "UTF-8"), readFileLines("large.csv", "UTF-8", 1).trim()},
                {"https://data.consumerfinance.gov/file.xml", 0, "XML", readFile("large.csv", "UTF-8"), readFileBytes("large.csv", "UTF-8", ParserService.HEADER_LENGTH)},
                {"https://not_supported_file_extension.org/test.AVR", 0, "AVR", readFile("sample2.json", "UTF-8"), readFileBytes("sample2.json", "UTF-8",ParserService.HEADER_LENGTH)},
                {"https://wp.pl", 0, "HTML", readFile("sample_html.txt", "UTF-8"), readFileBytes("sample_html.txt", "UTF-8",ParserService.HEADER_LENGTH)},
                {"https://data.consumerfinance.gov", 5, "CSV", readFile("sample.csv", "UTF-8"), readFileLines("sample.csv", "UTF-8", 1).trim()},
                {"https://data.consumerfinance.gov", 0, "CSV", readFile("empty.csv", "UTF-8"), readFileLines("empty.csv", "UTF-8", 0).trim()},
                {"hdfs://nameservice1/org/guid/brokers/userspace/teststore/inobjectstore/plik", 0, "CSV", readFile("empty.csv", "UTF-8"), readFileLines("empty.csv", "UTF-8", 0).trim()},
                {"https://data.consumerfinance.gov/data.xml", 0, "XML", readFile("empty.xml", "UTF-8"), readFile("empty.xml", "UTF-8")},
        });
    }

    private final String sourceUri;
    private final long recordCount;
    private final String type;
    private final String content;
    private final String targetUri;
    private final String header;
    private final int size;
    
    public ParseServiceTest(String sourceUri, long recordCount, String type, String content, String header) {
        this.sourceUri = sourceUri;
        this.recordCount = recordCount;
        this.type = type;
        this.content = content;
        this.header = header;
        this.size = content.length();
        if (sourceUri.startsWith("hdfs://")) {
            this.targetUri = this.sourceUri;
        } else {
            this.targetUri = "teststore/inobjectstore";
        }
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
