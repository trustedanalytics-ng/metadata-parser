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
package org.trustedanalytics.metadata.security;

import org.apache.directory.api.util.exception.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

public class BufferedRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(BufferedRequestWrapper.class);

    private final String body;

    public BufferedRequestWrapper(HttpServletRequest innerRequest) {
        super(innerRequest);

        BufferedReader reader = null;
        try {
            reader = innerRequest.getReader();

            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            body = requestBody.toString();

        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
            throw new InvalidParameterException("Cannot parse request body");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LOG.debug(e.getMessage(), e);
            }
        }
    }

    @Override
    public ServletInputStream getInputStream(){

        final ByteArrayInputStream byteArrayInputStream =
            new ByteArrayInputStream(body.getBytes());

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                throw new NotImplementedException();
            }
            @Override
            public boolean isReady() {
                throw new NotImplementedException();
            }
            @Override
            public void setReadListener(ReadListener readListener) {
                throw new NotImplementedException();
            }
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    public String getBody() {
        return body;
    }
}
