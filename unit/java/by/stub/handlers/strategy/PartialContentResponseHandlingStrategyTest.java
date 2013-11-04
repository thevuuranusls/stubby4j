package by.stub.handlers.strategy;

import static by.stub.yaml.stubs.StubRequest.RANGE_HEADER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import by.stub.handlers.strategy.stubs.PartialContentResponseHandlingStrategy;
import by.stub.handlers.strategy.stubs.StubResponseHandlingStrategy;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PartialContentResponseHandlingStrategyTest {
    private static final int FILESIZE = 12543;
    private StubResponseHandlingStrategy subject;

    private StubResponse mockStubResponse;
    private StubRequest mockAssertionRequest;
    private HashMap<String, String> headerMap = new HashMap<String, String>();
    private File file;

    @Before
    public void before() throws Exception {
        mockStubResponse = mock(StubResponse.class);
        mockAssertionRequest = mock(StubRequest.class);

        file = File.createTempFile("stubby", "test");
        FileOutputStream fos = new FileOutputStream(file);
        for (int i = 0; i < FILESIZE; i++) {
            fos.write(i);
        }
        fos.close();


        when(mockStubResponse.getRawFile()).thenReturn(file);
        when(mockAssertionRequest.getHeaders()).thenReturn(headerMap);

        subject = new PartialContentResponseHandlingStrategy(mockStubResponse);
    }

    @Test(expected = IOException.class)
    public void shouldRespondWithErrorIfRangeRequestIsNotSet() throws Exception {
        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        subject.handle(mockHttpServletResponse, mockAssertionRequest);
    }

    @Test
    public void shouldRespondWithStatusCode206() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-1");
        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.PARTIAL_CONTENT_206);
    }

    @Test
    public void shouldRespondWithCorrectHeaders() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-1");
        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.SERVER, HandlerUtils.constructHeaderServerName());
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CONTENT_TYPE, MimeTypes.TEXT_HTML_UTF_8);
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        verify(mockHttpServletResponse, times(1)).setHeader(HttpHeaders.PRAGMA, "no-cache");
        verify(mockHttpServletResponse, times(1)).setDateHeader(HttpHeaders.EXPIRES, 0);
    }

    @Test
    public void shouldRespondWithCorrectContentHeader() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-1");
        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        verify(mockHttpServletResponse, times(1)).setHeader("Content-Range", "bytes 0-1/"+FILESIZE);
    }

    @Test
    public void shouldRespondWithCorrectContentHeaderIfRangeHeaderIsLowerCase() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-1");
        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);
        when(mockHttpServletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        verify(mockHttpServletResponse, times(1)).setStatus(HttpStatus.PARTIAL_CONTENT_206);
    }

    @Test
    public void shouldOnlyRespondWithRequestedByteRange() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-1");

        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);

        final List<Integer> data = new ArrayList<Integer>();
        when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(final int i) throws IOException {
                data.add(i);
            }
        });

        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        assertEquals(2, data.size());
        assertEquals(Arrays.asList(0, 1), data);
    }


    @Test
    public void shouldOnlyRespondWithRequestedByteRangeWholeFile() throws Exception {
        headerMap.put(RANGE_HEADER, "bytes=0-");

        final HttpServletResponseWithGetStatus mockHttpServletResponse = mock(HttpServletResponseWithGetStatus.class);

        final List<Integer> data = new ArrayList<Integer>();
        when(mockHttpServletResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(final int i) throws IOException {
                data.add(i);
            }
        });

        subject.handle(mockHttpServletResponse, mockAssertionRequest);
        assertEquals(FILESIZE, data.size());
    }
}
