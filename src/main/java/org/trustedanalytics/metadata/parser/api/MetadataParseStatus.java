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
package org.trustedanalytics.metadata.parser.api;

import java.util.Objects;


public class MetadataParseStatus {

    public enum State {
        DONE,
        FAILED
    }
    
    private State state;
    private String description;
    
    public MetadataParseStatus() {
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MetadataParseStatus [state=" + state + ", description=" + description + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, description);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetadataParseStatus other = (MetadataParseStatus) obj;
        return Objects.equals(this.state, other.state) &&
            Objects.equals(this.description, other.description);
    }

    public static MetadataParseStatus done() {
        MetadataParseStatus status = new MetadataParseStatus();
        status.state = MetadataParseStatus.State.DONE;
        return status;
    }

    public static MetadataParseStatus failed() {
        MetadataParseStatus status = new MetadataParseStatus();
        status.state = MetadataParseStatus.State.FAILED;
        return status;
    }

}
