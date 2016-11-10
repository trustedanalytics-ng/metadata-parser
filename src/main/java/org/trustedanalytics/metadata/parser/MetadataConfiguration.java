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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.cloud.auth.OAuth2TokenRetriever;
import org.trustedanalytics.hadoop.config.client.Configurations;
import org.trustedanalytics.hadoop.config.client.ServiceInstanceConfiguration;
import org.trustedanalytics.metadata.filesystem.HdfsConfigProvider;
import org.trustedanalytics.metadata.filesystem.HdfsConfigProviderFromEnv;
import org.trustedanalytics.store.EnableObjectStore;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableObjectStore
@Configuration
public class MetadataConfiguration {

  @Bean
  public ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  @Bean
  public AuthTokenRetriever authTokenRetriever() {
    return new OAuth2TokenRetriever();
  }

  @Bean
  @Profile({"cloud"})
  public HdfsConfigProvider vcapHdfsConfigProvider() throws IOException {
    return new HdfsConfigProviderFromEnv(Configurations.newInstanceFromEnv());
  }
  
  @Bean
  @Profile({"kubernetes"})
  public HdfsConfigProvider k8sHdfsConfigProvider(ServiceInstanceConfiguration hdfsConfig) {
    return new HdfsConfigProviderFromEnv(hdfsConfig);
  }
}
