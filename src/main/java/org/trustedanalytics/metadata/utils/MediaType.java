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

import java.util.Optional;

public enum MediaType { 
    
    XML("application/xml", new String [] {"XML"}, "XML"),
    HTML("text/html", new String [] {"HTML", "HTM"}, "HTML"),
    CSV("text/csv", new String [] {"CSV"}, "CSV") ,
    JSON("application/json", new String [] {"JSON"}, "JSON");
    
    final String value;
    final String extensions[];
    final String humanFriendlyFormat;
    
    MediaType(String value, String extensions[], String humanFriendlyFormat) {
        this.value = value;
        this.extensions = extensions;
        this.humanFriendlyFormat = humanFriendlyFormat;
    }
    
    public String toString() {
        return value;
    }

    public String getHumanFriendlyFormat() { return humanFriendlyFormat; }
    
    public static Optional<MediaType> fromExtension(String x) {
        for (MediaType val : MediaType.values()) {
            if (hasExtension(x, val) ) {
                return Optional.of(val);
            }
        }
        return Optional.empty();
    }
    
    public static Optional<MediaType> fromString(String contentType) {
        for (MediaType type : MediaType.values()) {
            if (type.value.equals(contentType) ) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    private static boolean hasExtension(String x, MediaType val) {
        for (String ex: val.extensions) {
            if (ex.equals(x)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean is(String other) {
        return this.humanFriendlyFormat.equals(other);
    }
    
}