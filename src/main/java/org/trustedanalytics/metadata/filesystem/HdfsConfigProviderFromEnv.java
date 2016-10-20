/**
 * Copyright (c) 2016 Intel Corporation
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
package org.trustedanalytics.metadata.filesystem;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.hadoop.config.client.AppConfiguration;
import org.trustedanalytics.hadoop.config.client.Property;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.hadoop.config.client.ServiceType;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;

public class HdfsConfigProviderFromEnv implements HdfsConfigProvider {

    private static final String AUTHENTICATION_METHOD = "kerberos";
    private static final String AUTHENTICATION_METHOD_PROPERTY = "hadoop.security.authentication";

    @Getter
    private final ServiceInstanceConfiguration hdfsConf;
    @Getter
    private ServiceInstanceConfiguration krbConf;
    @Getter
    private String kdc;
    @Getter
    private String realm;
    @Getter
    private final Configuration hadoopConf;

    public HdfsConfigProviderFromEnv(ServiceInstanceConfiguration hdfsConf) {
      this.hdfsConf = hdfsConf;
      kdc = hdfsConf.getProperty(Property.KRB_KDC).orElse(null);
      realm = hdfsConf.getProperty(Property.KRB_REALM).orElse(null);
      hadoopConf = hdfsConf.asHadoopConfiguration();
    }

    public HdfsConfigProviderFromEnv(AppConfiguration appConfiguration) throws IOException {
        hdfsConf = appConfiguration.getServiceConfig(ServiceType.HDFS_TYPE);
        krbConf = appConfiguration.getServiceConfig("kerberos-service");
        kdc = krbConf.getProperty(Property.KRB_KDC).get();
        realm = krbConf.getProperty(Property.KRB_REALM).get();
        hadoopConf = hdfsConf.asHadoopConfiguration();
    }

    @Override
    public boolean isKerberosEnabled() {
        return AUTHENTICATION_METHOD.equals(hadoopConf.get(AUTHENTICATION_METHOD_PROPERTY));
    }

    @Override
    public String getHdfsUri() {
        return hadoopConf.get("fs.defaultFS");
    }

    @Override
    public String getHdfsOrgUri(UUID org) {
        return PathTemplate.resolveOrg(hdfsConf.getProperty(Property.HDFS_URI).get(), org);
    }

    private static class PathTemplate {
        private static final String ORG_PLACEHOLDER = "organization";
        private static final String PLACEHOLDER_PREFIX = "%{";
        private static final String PLACEHOLDER_SUFIX = "}";

        private PathTemplate() {
        }

        private static String resolveOrg(String url, UUID org) {
            ImmutableMap<String, UUID> values = ImmutableMap.of(ORG_PLACEHOLDER, org);
            return new StrSubstitutor(values, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFIX).replace(url);
        }
    }

}
