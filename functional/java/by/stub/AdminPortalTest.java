package by.stub;

import by.stub.builder.yaml.YamlBuilder;
import by.stub.cli.ANSITerminal;
import by.stub.client.StubbyClient;
import by.stub.client.StubbyResponse;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubResponse;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;

public class AdminPortalTest {

   private static final int STUBS_PORT = 5992;
   private static final int STUBS_SSL_PORT = 5993;
   private static final int ADMIN_PORT = 5999;

   private static final String STUBS_URL = String.format("http://localhost:%s", STUBS_PORT);
   private static final String ADMIN_URL = String.format("http://localhost:%s", ADMIN_PORT);

   private static final StubbyClient STUBBY_CLIENT = new StubbyClient();
   private static String stubsData;

   @BeforeClass
   public static void beforeClass() throws Exception {

      ANSITerminal.muteConsole(true);

      final URL url = AdminPortalTest.class.getResource("/yaml/stubs.yaml");
      final InputStream stubsDatanputStream = url.openStream();
      stubsData = StringUtils.inputStreamToString(stubsDatanputStream);
      stubsDatanputStream.close();

      STUBBY_CLIENT.startJetty(STUBS_PORT, STUBS_SSL_PORT, ADMIN_PORT, url.getFile());
   }

   @Before
   public void beforeEach() throws Exception {
      final StubbyResponse adminPortalResponse = STUBBY_CLIENT.updateStubbedData(ADMIN_URL, stubsData);
      assertThat(adminPortalResponse.getResponseCode()).isEqualTo(HttpStatus.CREATED_201);
   }

   @AfterClass
   public static void afterClass() throws Exception {
      STUBBY_CLIENT.stopJetty();
   }

   @Test
   public void should_ReturnMethodNotImplemented_WhenSuccessfulOptionsMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.OPTIONS, requestUrl);

      final HttpResponse httpResponse = httpPutRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
      assertThat(statusMessage).isEqualTo("Not Implemented");
      assertThat(responseMessage).isEqualTo("Method OPTIONS is not implemented on URI /");
   }

   @Test
   public void should_ReturnMethodNotImplemented_WhenSuccessfulHeadMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.HEAD, requestUrl);

      final HttpResponse httpResponse = httpPutRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
      assertThat(statusMessage).isEqualTo("Not Implemented");
      assertThat(responseMessage).isEqualTo("");
   }

   @Test
   public void should_ReturnMethodNotImplemented_WhenSuccessfulTraceMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.TRACE, requestUrl);

      final HttpResponse httpResponse = httpPutRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED_501);
      assertThat(statusMessage).isEqualTo("Not Implemented");
      assertThat(responseMessage).isEqualTo("Method TRACE is not implemented on URI /");
   }

   @Test
   public void shouldMakeSuccessfulGetRequestToStatusPage() throws Exception {

      final String requestUrl = String.format("%s%s", ADMIN_URL, "/status");
      final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse httpResponse = httpGetRequest.execute();
      final String responseContentAsString = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(responseContentAsString).contains("Live Tweaks to YAML Configuration");
      assertThat(responseContentAsString).contains("/pdf/hello-world");
      assertThat(responseContentAsString).contains("STATUS");
      assertThat(responseContentAsString).contains("/uri/with/single/sequenced/response");
   }

   @Test
   public void should_ReturnAllStubbedRequestsAsYAML_WhenSuccessfulGetMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s%s", ADMIN_URL, "/");
      final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse httpResponse = httpGetRequest.execute();
      final String responseContentAsString = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(responseContentAsString).contains("request");
      assertThat(responseContentAsString).contains("url: ^/resources/asn/");
      assertThat(responseContentAsString).contains("content-disposition: attachment; filename=hello-world.pdf");
      assertThat(responseContentAsString).contains("file: ../json/response.json");
      assertThat(responseContentAsString).contains("url: /uri/with/single/sequenced/response");
   }

   @Test
   public void should_ReturnSingleStubbedRequestAsYAML_WhenSuccessfulGetMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

      final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
      final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse httpResponse = httpGetRequest.execute();
      final String responseContentAsString = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(responseContentAsString).containsOnlyOnce("request");
      assertThat(responseContentAsString).containsOnlyOnce("url: ^/resources/asn/");
      assertThat(responseContentAsString).containsOnlyOnce("response");
      assertThat(responseContentAsString).containsOnlyOnce("content-type: application/json");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulGetMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

      final int invalidIndex = 88888888;
      final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
      final HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse httpResponse = httpGetRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();

      final String expectedMessage = String.format("Stub request index#%s does not exist, cannot display", invalidIndex);

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(statusMessage).isEqualTo(expectedMessage);
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulPutMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

      final HttpResponse httpResponse = httpPutRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
      assertThat(statusMessage).isEqualTo("Method Not Allowed");
      assertThat(responseMessage).isEqualTo("Method PUT is not allowed on URI /");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulEmptyPutMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/1", ADMIN_URL);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(statusMessage).isEqualTo("PUT request on URI /1 was empty");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulPutMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

      final int invalidIndex = 88888888;
      final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();

      final String expectedMessage = String.format("Stub request index#%s does not exist, cannot update", invalidIndex);

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(statusMessage).isEqualTo(expectedMessage);
   }

   @Test
   public void should_UpdateStubbedRequest_WhenSuccessfulPutMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

      final String requestUrl = String.format("%s%s", ADMIN_URL, "/0");
      HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
      HttpResponse httpGetResponse = httpGetRequest.execute();
      String getResponseContent = httpGetResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
      assertThat(getResponseContent).containsOnlyOnce("request");
      assertThat(getResponseContent).containsOnlyOnce("url: ^/resources/asn/");
      assertThat(getResponseContent).containsOnlyOnce("response");
      assertThat(getResponseContent).containsOnlyOnce("content-type: application/json");

      final String yamlToUpdate = new YamlBuilder()
         .newStubbedRequest()
         .withUrl("^/resources/something/new")
         .withMethodGet()
         .withQuery("someKey", "someValue")
         .newStubbedResponse()
         .withHeaderContentType("application/xml")
         .withLiteralBody("OK")
         .withStatus("201")
         .build();

      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.PUT, requestUrl, yamlToUpdate);

      final HttpResponse httpPutResponse = httpPutRequest.execute();
      final String putResponseContent = httpPutResponse.parseAsString().trim();
      final String putResponseLocationHeader = httpPutResponse.getHeaders().getLocation();

      assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
      assertThat(putResponseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
      assertThat(putResponseContent).isEqualTo("Stub request index#0 updated successfully");


      httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
      httpGetResponse = httpGetRequest.execute();
      getResponseContent = httpGetResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
      assertThat(getResponseContent).containsOnlyOnce("request");
      assertThat(getResponseContent).containsOnlyOnce("query");
      assertThat(getResponseContent).containsOnlyOnce("url: ^/resources/something/new");
      assertThat(getResponseContent).containsOnlyOnce("response");
      assertThat(getResponseContent).containsOnlyOnce("content-type: application/xml");

      assertThat(getResponseContent).doesNotContain("url: ^/resources/asn/");
      assertThat(getResponseContent).doesNotContain("content-type: application/json");
   }


   @Test
   public void should_ReturnExpectedError_WhenSuccessfulDeleteMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPutRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

      final HttpResponse httpResponse = httpPutRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
      assertThat(statusMessage).isEqualTo("Method Not Allowed");
      assertThat(responseMessage).isEqualTo("Method DELETE is not allowed on URI /");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulDeleteMade_ToAdminPortalRootWithInvalidIndexURI() throws Exception {

      final int invalidIndex = 88888888;
      final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();

      final String expectedMessage = String.format("Stub request index#%s does not exist, cannot delete", invalidIndex);

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(statusMessage).isEqualTo(expectedMessage);
   }

   @Test
   public void should_DeleteStubbedRequest_WhenSuccessfulDeleteMade_ToAdminPortalRootWithValidIndexURI() throws Exception {

      final String requestUrl = String.format("%s%s", ADMIN_URL, "/2");
      HttpRequest httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
      HttpResponse httpGetResponse = httpGetRequest.execute();
      String getResponseContent = httpGetResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
      assertThat(getResponseContent).containsOnlyOnce("request");
      assertThat(getResponseContent).containsOnlyOnce("url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}");
      assertThat(getResponseContent).containsOnlyOnce("response");
      assertThat(getResponseContent).containsOnlyOnce("content-type: application/json");


      final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, requestUrl);

      final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
      final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
      assertThat(deleteResponseContent).isEqualTo("Stub request index#2 deleted successfully");

      httpGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);
      httpGetResponse = httpGetRequest.execute();
      getResponseContent = httpGetResponse.parseAsString().trim();

      assertThat(HttpStatus.OK_200).isEqualTo(httpGetResponse.getStatusCode());
      assertThat(getResponseContent).doesNotContain("url: ^/[a-z]{3}-[a-z]{3}/[0-9]{2}/[A-Z]{2}/[a-z0-9]+\\?paramOne=[a-zA-Z]{3,8}&paramTwo=[a-zA-Z]{3,8}");
   }


   @Test
   public void should_ReturnExpectedError_WhenSuccessfulPostMade_ToAdminPortalRootWithAnIndexURI() throws Exception {

      final int invalidIndex = 5;
      final String requestUrl = String.format("%s/%s", ADMIN_URL, invalidIndex);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED_405);
      assertThat(statusMessage).isEqualTo("Method Not Allowed");
      assertThat(responseMessage).isEqualTo("Method POST is not allowed on URI /5");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulEmptyPostMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT_204);
      assertThat(statusMessage).isEqualTo("POST request on URI / was empty");
   }

   @Test
   public void should_ReturnExpectedError_WhenSuccessfulInvalidPostMade_ToAdminPortalRoot() throws Exception {

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, "unparseable rubbish post content");

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      final String expectedMessage = "Problem handling request in Admin handler: java.io.IOException: Loaded YAML root node must be an instance of ArrayList, otherwise something went wrong. Check provided YAML";

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
      assertThat(statusMessage).isEqualTo(expectedMessage);
      assertThat(responseMessage).contains(expectedMessage);
   }

   @Test
   public void should_UpdateStubsData_WhenSuccessfulValidPostMade_ToAdminPortalRoot() throws Exception {

      final String yamlToUpdate = new YamlBuilder()
         .newStubbedRequest()
         .withUrl("^/resources/something/new")
         .withMethodGet()
         .withQuery("someKey", "someValue")
         .newStubbedResponse()
         .withHeaderContentType("application/xml")
         .withLiteralBody("OK")
         .withStatus("201")
         .build();

      final String requestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, yamlToUpdate);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();
      final String responseLocationHeader = httpResponse.getHeaders().getLocation();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat(responseLocationHeader).isEqualTo("^/resources/something/new?someKey=someValue");
      assertThat(statusMessage).isEqualTo("Created");
      assertThat(responseMessage).contains("Configuration created successfully");
   }

   @Test
   public void should_UpdateStubsDataAndGetNewResource_WhenSuccessfulValidPostMade_ToAdminPortalRoot() throws Exception {

      final String yamlToUpdate = new YamlBuilder()
         .newStubbedRequest()
         .withUrl("^/new/resource/.*$")
         .withMethodGet()
         .withQuery("someKey", "someValue")
         .newStubbedResponse()
         .withHeaderContentType("application/xml")
         .withLiteralBody("OK")
         .withStatus("200")
         .build();

      final String adminRequestUrl = String.format("%s/", ADMIN_URL);
      final HttpRequest httpPuttRequest = HttpUtils.constructHttpRequest(HttpMethods.POST, adminRequestUrl, yamlToUpdate);

      final HttpResponse httpResponse = httpPuttRequest.execute();
      final String statusMessage = httpResponse.getStatusMessage().trim();
      final String responseMessage = httpResponse.parseAsString().trim();

      assertThat(httpResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);
      assertThat(statusMessage).isEqualTo("Created");
      assertThat(responseMessage).contains("Configuration created successfully");

      final String stubsRequestUrl = String.format("%s%s", STUBS_URL, "/new/resource/account?someKey=someValue");
      final HttpRequest stubsGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
      final HttpResponse stubsGetResponse = stubsGetRequest.execute();

      final String stubsGetResponseContentTypeHeader = stubsGetResponse.getContentType();
      final String stubsGetResponseContent = stubsGetResponse.parseAsString().trim();

      assertThat(stubsGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(stubsGetResponseContent).isEqualTo("OK");
      assertThat(stubsGetResponseContentTypeHeader).contains("application/xml");
   }

   @Test
   public void should_AdjustResourceIdHeaderAccordingly_WhenSuccessfulDeleteMade() throws Exception {

      final String stubsRequestUrl = String.format("%s%s", STUBS_URL, "/this/stub/should/always/be/second/in/this/file");
      final HttpRequest stubsGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
      final HttpResponse preDeletionStubGetResponse = stubsGetRequest.execute();
      final HttpHeaders preDeletionResponseHeaders = preDeletionStubGetResponse.getHeaders();
      assertThat(preDeletionResponseHeaders.containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isTrue();
      assertThat(preDeletionResponseHeaders.getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo("1");

      final HttpRequest httpDeleteRequest = HttpUtils.constructHttpRequest(HttpMethods.DELETE, String.format("%s%s", ADMIN_URL, "/0"));
      final HttpResponse httpDeleteResponse = httpDeleteRequest.execute();
      final String deleteResponseContent = httpDeleteResponse.parseAsString().trim();
      assertThat(HttpStatus.OK_200).isEqualTo(httpDeleteResponse.getStatusCode());
      assertThat(deleteResponseContent).isEqualTo("Stub request index#0 deleted successfully");

      final HttpRequest postDeletionStubGetRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, stubsRequestUrl);
      final HttpResponse postDeletionStubGetResponse = postDeletionStubGetRequest.execute();
      final HttpHeaders postDeletionResponseHeaders = postDeletionStubGetResponse.getHeaders();
      assertThat(postDeletionResponseHeaders.containsKey(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isTrue();
      assertThat(postDeletionResponseHeaders.getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER)).isEqualTo("0");
   }

   @Test
   public void should_ReturnAjaxRequestContent_WhenSuccessfulRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
      final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

      final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
      final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/request/post");
      final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

      final HttpResponse ajaxResponse = ajaxRequest.execute();
      assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(ajaxResponse.parseAsString().trim()).isEqualTo(postContent);
   }

   @Test
   public void should_ReturnAjaxResponseContent_WhenSuccessfulRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_URL, "/invoice/new");
      final String postContent = "{\"name\": \"chocolate\", \"description\": \"full\", \"department\": \"savoury\"}";
      final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.POST, requestUrl, postContent);

      final HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.setContentType("application/json");

      request.setHeaders(httpHeaders);

      final HttpResponse response = request.execute();
      final String responseContent = response.parseAsString().trim();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

      final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
      final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/response/body");
      final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

      final HttpResponse ajaxResponse = ajaxRequest.execute();
      assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(ajaxResponse.parseAsString().trim()).isEqualTo(responseContent);
   }

   @Test
   public void should_ReturnAjaxSequencedResponseContent_WhenSuccessfulRequestMade() throws Exception {

      final String requestUrl = String.format("%s%s", STUBS_URL, "/uri/with/sequenced/responses/infile");
      final HttpRequest request = HttpUtils.constructHttpRequest(HttpMethods.GET, requestUrl);

      final HttpResponse response = request.execute();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED_201);

      final String resourceID = response.getHeaders().getFirstHeaderStringValue(StubResponse.STUBBY_RESOURCE_ID_HEADER);
      final String ajaxRequestUrl = String.format("%s%s%s%s", ADMIN_URL, "/ajax/resource/", resourceID, "/response/1/file");
      final HttpRequest ajaxRequest = HttpUtils.constructHttpRequest(HttpMethods.GET, ajaxRequestUrl);

      final HttpResponse ajaxResponse = ajaxRequest.execute();
      assertThat(ajaxResponse.getStatusCode()).isEqualTo(HttpStatus.OK_200);
      assertThat(ajaxResponse.parseAsString().trim()).isEqualTo("Still going strong!");
   }
}