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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.cloud.auth.AuthTokenRetriever;
import org.trustedanalytics.cloud.auth.HeaderAddingHttpInterceptor;
import org.trustedanalytics.metadata.Main;
import org.trustedanalytics.metadata.datacatalog.DataCatalogFactory;
import org.trustedanalytics.metadata.parser.api.Metadata;
import org.trustedanalytics.metadata.parser.api.MetadataParseRequest;
import org.trustedanalytics.metadata.parser.api.MetadataParseStatus;
import org.trustedanalytics.metadata.parser.api.MetadataParserCallback;
import org.trustedanalytics.metadata.security.authorization.Authorization;
import org.trustedanalytics.store.MemoryObjectStore;
import org.trustedanalytics.store.ObjectStoreFactory;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Ignore;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Main.class, MetadataParserIT.TestConfig.class})
@WebAppConfiguration
@IntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"test"})
public class MetadataParserIT {

    @Value("http://localhost:${local.server.port}")
    private String baseUrl;

    @Value("${dependencies.datacatalog}:${local.server.port}")
    private String dataCatalogUrl;

    @Autowired
    private String TOKEN;
    @Autowired
    private ObjectStoreFactory<UUID> objectStoreFactory;
    @Autowired
    private MetadataParserCallback callback;
    @Autowired
    private AuthTokenRetriever tokenRetriever;
    @Autowired
    private CompletableFuture<Metadata> putMetadata;
    @Autowired
    private RuntimeConfigurableDataCatalogFactory dataCatalogFactory;

    private String idInStore;
    private TestRestTemplate testRestTemplate;
    private MetadataParseRequest request;

    private String content;
    private String source;

    @Autowired
    private Authorization authorization;

    private final static UUID TEST_ORG_UUID =
        UUID.fromString("f02e100b-8390-463a-8165-180ea4dd88ee");

    @Before
    public void before() throws IOException, LoginException, InterruptedException {

        dataCatalogFactory.setDataCatalogUrl(dataCatalogUrl);

        source = "http://data.com/1234.csv";
        content = "testheader\ntestrow";

        when(tokenRetriever.getAuthToken(any(Authentication.class))).thenReturn(TOKEN);
        idInStore = objectStoreFactory.create(null).save(content.getBytes());
        testRestTemplate = new TestRestTemplate();
        request = new MetadataParseRequest();
    }

    private void authorizationAlways(boolean success) throws IOException, ServletException {
        when(authorization.checkAccess(any(), any())).thenReturn(success);
    }

    @Ignore
    @Test
    public void parseMetadata_existingDataset()
        throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException,
        IOException, ServletException {

        authorizationAlways(true);

        request.setTitle("title");
        request.setId("1234");
        request.setIdInObjectStore(idInStore);
        request.setSource(source);
        request.setCallbackUrl(new URI(baseUrl + "/callbacks/" + request.getId()));
        request.setOrgUUID(TEST_ORG_UUID);

        assertThat(postRequest(request,"/rest/datasets").getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(putMetadata.get(500, TimeUnit.MILLISECONDS), equalTo(metadata()));

        verify(callback, timeout(500))
            .statusUpdate(Matchers.eq(MetadataParseStatus.done()), eq(request.getId()));
        verify(authorization)
            .checkAccess(any(HttpServletRequest.class), eq(TEST_ORG_UUID));
    }

    @Test
    public void parseMetadata_accessForbidden()
        throws URISyntaxException, IOException, ServletException {

        authorizationAlways(false);

        request.setTitle("title");
        request.setId("1234");
        request.setIdInObjectStore(idInStore);
        request.setSource(source);
        request.setCallbackUrl(new URI(baseUrl + "/callbacks/" + request.getId()));
        request.setOrgUUID(TEST_ORG_UUID);
        request.setPublicRequest(false);

        assertThat(postRequest(request).getStatusCode(), equalTo(HttpStatus.FORBIDDEN));

        verify(authorization)
            .checkAccess(any(HttpServletRequest.class), eq(TEST_ORG_UUID));
    }

    @Test
    public void parseMetadata_notExistingDataset()
        throws URISyntaxException, IOException, ServletException {

        authorizationAlways(true);

        request.setId("1234");
        request.setIdInObjectStore("not_existing_id");
        request.setCallbackUrl(new URI(baseUrl + "/callbacks/" + request.getId()));
        request.setOrgUUID(TEST_ORG_UUID);
        request.setSource(source);


        assertThat(postRequest(request).getStatusCode(), equalTo(HttpStatus.ACCEPTED));

        verify(callback, timeout(500))
            .statusUpdate(eqState(MetadataParseStatus.State.FAILED), eq(request.getId()));

        verify(authorization)
            .checkAccess(any(HttpServletRequest.class), eq(TEST_ORG_UUID));
    }

    private Metadata metadata() {
        Metadata metadata = new Metadata();
        metadata.setDataSample("testheader");
        metadata.setSourceUri(source);
        metadata.setFormat("CSV");
        metadata.setTargetUri("in_memory/1");
        metadata.setTitle("title");
        metadata.setSize(content.length());
        metadata.setOrgUUID(TEST_ORG_UUID);

        return metadata;
    }

    private ResponseEntity<String> postRequest(MetadataParseRequest request) {
        testRestTemplate.setInterceptors(Collections.singletonList(new HeaderAddingHttpInterceptor(
            HttpHeaders.AUTHORIZATION, "bearer " + TOKEN)));

        return testRestTemplate.postForEntity(baseUrl + "/rest/metadata", request, String.class);
    }
    private ResponseEntity<String> postRequest(MetadataParseRequest request, String endpoint) {
        testRestTemplate.setInterceptors(Collections.singletonList(new HeaderAddingHttpInterceptor(
            HttpHeaders.AUTHORIZATION, "bearer " + TOKEN)));

        return testRestTemplate.postForEntity(baseUrl + endpoint, request, String.class);
    }

    private MetadataParseStatus eqState(MetadataParseStatus.State state) {
        return Mockito.argThat(new StatusMatcher(state));
    }

    @Configuration
    @Profile({"test"})
    public static class TestConfig {

        @Bean
        public ObjectStoreFactory<UUID> objectStoreFactory() {
            return (x) -> new MemoryObjectStore();
        }

        @Bean
        public RestOperations dataCatalogTemplate() {
            return mock(RestOperations.class);
        }

        @Bean
        public MetadataParserCallback callback() {
            return mock(MetadataParserCallback.class);
        }

        @Bean
        public CompletableFuture<Metadata> putMetadata() {
            return new CompletableFuture<>();
        }

        @Bean
        public String TOKEN() {
            return "jhksdf8723kjhdfsh4i187y91hkajl";
        }

        @Bean
        public AuthTokenRetriever authTokenRetriever() {
            return mock(AuthTokenRetriever.class);
        }

        @Bean
        public BeanPostProcessor authenticationDisabler() {
            return new AuthenticationDisabler();
        }

        @Bean
        public Authorization authorization() throws IOException, ServletException {
            return mock(Authorization.class);
        }

        @Bean
        public DataCatalogFactory dataCatalogFactory() throws IOException, ServletException {
            return new RuntimeConfigurableDataCatalogFactory();
        }
    }


    // Act as a host waiting for callback
    @RestController
    public static class CallbackController {

        @Autowired
        private MetadataParserCallback callback;

        @RequestMapping(value = "/callbacks/{id}", method = RequestMethod.POST)
        public void statusUpdate(@RequestBody MetadataParseStatus status,
            @PathVariable("id") String id) {
            callback.statusUpdate(status, id);
        }
    }


    // Act as s host waiting for metadata
    @RestController
    public static class DatasetController {

        @Autowired
        private CompletableFuture<Metadata> putMetadata;

        @RequestMapping(value = "/rest/datasets", method = RequestMethod.POST)
        public void putMetadata(@RequestBody Metadata metadata) {
            System.out.println("Complete :" + metadata);
            putMetadata.complete(metadata);
        }
    }


    public static class StatusMatcher extends ArgumentMatcher<MetadataParseStatus> {

        private final MetadataParseStatus.State state;

        public StatusMatcher(MetadataParseStatus.State state) {
            this.state = state;
        }

        @Override
        public boolean matches(Object argument) {
            return ((MetadataParseStatus) argument).getState() == state;
        }
    }
}
