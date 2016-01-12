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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class ContentDetectionUtils {

    public static final int MAX_BYTES_READ_WHILE_PROBING_TYPE = 2048;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentDetectionUtils.class);

    public static String bestGuessFileType(BufferedInputStream bin, String fileUri) throws IOException {
        
        Optional<MediaType> detectedType = Optional.empty();
        String fileExtension = getFileTypeFromExtension(fileUri);
        
        LOGGER.info("File extension : " + fileExtension);
        
        if (isMeaningfulExtension(fileExtension)) {
            
            detectedType = MediaType.fromExtension(fileExtension);
            LOGGER.info("Media type from extension:" + detectedType);
            
            if (!detectedType.isPresent()) {
                // If we are here, it means there is a meaningful file extension,
                // but's not supported by our MediaType enum.
                LOGGER.info("Using new file type:" + fileExtension);
                return fileExtension;
            }
            
        } else {
            LOGGER.info("Stream type guessing begins..");
            detectedType = guessContentFromStream(bin);    
        }
        
        LOGGER.info("File type detected: " + detectedType);

        if (!detectedType.isPresent()) {    
            LOGGER.info("Unable to detect format from extension or content. Assuming CSV type..");
        }
        
        return detectedType.orElse(MediaType.CSV).getHumanFriendlyFormat();
    }
    
    private static boolean isMeaningfulExtension(String extension) { 
        return extension != null && !ImmutableList.of("GZ","ZIP").contains(extension);
    }

    private static boolean canBeJson(String buffer) {
        LOGGER.info("Counting {} brackets");
        Integer[] counted =  countBrackets("{}", buffer);
        return passingJsonCriteria(counted);
    }
    
    /*
     *
     * Simple JSON matching criteria. Feel free to improve algorithm
     * and don't forget to add unit tests. 
     * We expect at least 4 '{' or '}' brackets.
     * We also expect that half of it is paired. 
     * 
     */
    private static boolean passingJsonCriteria(Integer[] counted) {
        final int totalIdx = 0;
        final int pairedIdx = 1;
        final int totalThreshold = 4;
        final double pairedFactor = 0.5;
        
        return counted[totalIdx] >= totalThreshold &&
            counted[pairedIdx] > (counted[totalIdx]*pairedFactor);
    }
    
    private static Integer[] countBrackets(String brackets, String buffer) {
        
        int opening = 0;
        int closing = 0;
        
        LOGGER.debug("Buffer size = {}, buffer = {}", buffer.length(),  buffer);
        for (byte x : buffer.getBytes()) {
            if (brackets.charAt(0) == x)  {
                opening++;
            } else 
            if (brackets.charAt(1) == x)  {
                closing++;
            }
        }
        /*
         *  Little explanation:
         *  we count number of pairs and multiply it by 2 because
         *  each pair contains two brackets.
         */
        int paired = (opening > closing ? closing : opening) * 2;
       
        int totalCount = opening + closing;
        
        LOGGER.info("Total count = {},  paired = {}", totalCount, paired);
        return new Integer[]{totalCount, paired};
        
    }
    
    private static Optional<MediaType> guessContentFromStream(BufferedInputStream bin)
            throws IOException {
        Optional<MediaType> type = notConsumingGuessContentTypeFromStream(bin);
        LOGGER.info("Guessed type from stream: " + type);
        if (!type.isPresent() && notConsumingDetectJsonInStream(bin)) {
             type = Optional.of(MediaType.JSON);
             LOGGER.info("Detected: " + type);
        }
        return type;
    }

    private static Optional<MediaType> notConsumingGuessContentTypeFromStream(
            BufferedInputStream bin) throws IOException {
        Optional<MediaType> type = Optional.empty();
        bin.mark(MAX_BYTES_READ_WHILE_PROBING_TYPE);
        try {
            String guess = URLConnection.guessContentTypeFromStream(bin);
            if (guess != null) {
                type = MediaType.fromString(guess);
            }
        } catch (IOException e){
            LOGGER.error("Error while guessing stream type",e);
        }         
        bin.reset();
        bin.mark(0);
        return type;
    }
    
    private static boolean notConsumingDetectJsonInStream(BufferedInputStream in) {
        byte[] bytes = new byte[MAX_BYTES_READ_WHILE_PROBING_TYPE];
        boolean ret = false;
        in.mark(MAX_BYTES_READ_WHILE_PROBING_TYPE);
        try {
            int bytesRead = in.read(bytes, 0, MAX_BYTES_READ_WHILE_PROBING_TYPE);
            if (bytesRead > 0 && canBeJson(new String(bytes))){
                ret = true;
            }
            in.reset();
        } catch (IOException e) {
            LOGGER.error("Error while guessing stream type",e);
        }
        in.mark(0);
        return ret;
    }
    
    public static String getFileTypeFromExtension(String uriStr) {
        String filename;
        try {
            filename = new URI(uriStr).getPath();   
        } catch (URISyntaxException e) {
            // assuming this is plain filename, not URI
            LOGGER.info("Assigning plain filename instead of URI",e);
            filename = uriStr;
        }
        
        return Optional.ofNullable(FilenameUtils.getExtension(filename)) 
        .filter(StringUtils::isNotBlank) 
        .map(String::toUpperCase)
        .orElse(null);        
    }
    
}
