package by.stub.yaml.stubs;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author: Alexander Zagniotov
 * Created: 4/20/13 5:29 PM
 */
public class StubHttpLifecycleTest {

   @Test
   public void shouldfindStubHttpLifecycleEqual_WhenComparedToItself() throws Exception {
      final StubHttpLifecycle expectedStubHttpLifecycle = new StubHttpLifecycle();

      final boolean assertionResult = expectedStubHttpLifecycle.equals(expectedStubHttpLifecycle);
      assertThat(assertionResult).isTrue();
   }

   @Test
   public void shouldfindStubHttpLifecycleNotEqual_WhenComparedToDifferentInstanceClass() throws Exception {
      final StubHttpLifecycle expectedStubHttpLifecycle = new StubHttpLifecycle();
      final Object assertingObject = StubResponse.newStubResponse();

      final boolean assertionResult = expectedStubHttpLifecycle.equals(assertingObject);
      assertThat(assertionResult).isFalse();
   }

   @Test
   public void shouldReturnStubResponse_WhenNoSequenceResponses() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(stubResponse);

      assertThat(stubHttpLifecycle.getActualStubbedResponse()).isEqualTo(stubResponse);
   }

   @Test
   public void shouldReturnDefaultstubResponse_WhenNoSequenceResponsePresentInTheList() throws Exception {

      final List<StubResponse> sequence = new LinkedList<StubResponse>();

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(sequence);

      final StubResponse actualStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();
      assertThat(actualStubbedResponse.getStatus()).isEqualTo("200");
      assertThat(actualStubbedResponse.getBody()).isEmpty();
   }

   @Test
   public void shouldReturnSequenceResponse_WhenOneSequenceResponsePresent() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

      final String expectedStatus = "200";
      final String expectedBody = "This is a sequence response #1";

      final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
         add(StubResponse.newStubResponse(expectedStatus, expectedBody));
      }};

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(sequence);

      final StubResponse actualStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();
      assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
      assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
   }

   @Test
   public void shouldReturnSecondSequenceResponseAfterSecondCall_WhenTwoSequenceResponsePresent() throws Exception {

      final StubResponse stubResponse = StubResponse.newStubResponse("201", "SELF");

      final String expectedStatus = "500";
      final String expectedBody = "This is a sequence response #2";

      final List<StubResponse> sequence = new LinkedList<StubResponse>() {{
         add(StubResponse.newStubResponse("200", "This is a sequence response #1"));
         add(StubResponse.newStubResponse(expectedStatus, expectedBody));
      }};

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(sequence);

      final StubResponse irrelevantStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();
      final StubResponse actualStubbedResponse = stubHttpLifecycle.getActualStubbedResponse();

      assertThat(actualStubbedResponse).isNotEqualTo(stubResponse);
      assertThat(actualStubbedResponse.getStatus()).isEqualTo(expectedStatus);
      assertThat(actualStubbedResponse.getBody()).isEqualTo(expectedBody);
   }

   @Test
   public void shouldReturnAjaxResponseContent_WhenStubTypeRequest() throws Exception {

      final String expectedPost = "this is a POST";
      final StubRequest stubRequest = StubRequest.newStubRequest("/some/resource/uri", expectedPost);

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setRequest(stubRequest);

      final String actualPost = stubHttpLifecycle.getAjaxResponseContent("request", "post");

      assertThat(expectedPost).isEqualTo(actualPost);
   }

   @Test
   public void shouldReturnAjaxResponseContent_WhenStubTypeResponse() throws Exception {

      final String expectedBody = "this is a response body";
      final StubResponse stubResponse = StubResponse.newStubResponse("201", expectedBody);

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      stubHttpLifecycle.setResponse(stubResponse);

      final String actualBody = stubHttpLifecycle.getAjaxResponseContent("response", "body");

      assertThat(expectedBody).isEqualTo(actualBody);
   }

   @Test
   public void shouldReturnAjaxResponseContent_WhenStubTypeUnknown() throws Exception {

      final StubHttpLifecycle stubHttpLifecycle = new StubHttpLifecycle();
      final String actualBody = stubHttpLifecycle.getAjaxResponseContent("blahblahblah", "body");

      assertThat("Unknown stub type: blahblahblah").isEqualTo(actualBody);
   }

    @Test
    public void shouldReturnIsRangeRequestWhenHeaderIsSet() throws Exception {
        final StubHttpLifecycle lifecycle = new StubHttpLifecycle();
        assertThat(lifecycle.isRangeRequest()).isFalse();

        StubRequest request = mock(StubRequest.class);
        lifecycle.setRequest(request);

        Map<String,String> headers = new HashMap<String, String>();
        when(request.getHeaders()).thenReturn(headers);

        assertThat(lifecycle.isRangeRequest()).isFalse();
        headers.put("range", "bytes=1-2");
        assertThat(lifecycle.isRangeRequest()).isTrue();
    }
}
