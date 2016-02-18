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

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.trustedanalytics.metadata.datacatalog.DataCatalog;
import org.trustedanalytics.metadata.datacatalog.DataCatalogFactory;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/rest/metadata")
public class MetadataParserController {

  private final ExecutorService executorService;
  private final ParseTaskFactory taskFactory;
  private final DataCatalogFactory dataCatalogFactory;
  private final RestOperationsFactory restOperationsFactory;

  @Autowired
  public MetadataParserController(ExecutorService executorService,
                                  ParseTaskFactory taskFactory,
                                  DataCatalogFactory dataCatalogFactory,
                                  RestOperationsFactory restOperationsFactory) {
    this.executorService = executorService;
    this.taskFactory = taskFactory;
    this.dataCatalogFactory = dataCatalogFactory;
    this.restOperationsFactory = restOperationsFactory;
  }

  @ApiOperation("Extracting basic information about dataset and adding it to datacatalog")
  @RequestMapping(method = POST)
  @ResponseStatus(ACCEPTED)
  public void extractMetadata(@RequestBody MetadataParseRequest request)
      throws HdfsRequestException, IOException, LoginException, InterruptedException {
    // Extracting of authentication needs to happen on thread that has access to request scope
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final RestOperations restOperations = restOperationsFactory.oAuth(authentication);
    final DataCatalog dataCatalog = dataCatalogFactory.get(restOperations);

    executorService.execute(taskFactory.newParseTask(request, dataCatalog, restOperations));
  }
}
