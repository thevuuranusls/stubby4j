package by.stub.handlers.strategy.stubs;

import by.stub.exception.Stubby4JException;
import by.stub.javax.servlet.http.HttpServletResponseWithGetStatus;
import by.stub.utils.HandlerUtils;
import by.stub.utils.StringUtils;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartialContentResponseHandlingStrategy implements StubResponseHandlingStrategy {
    private final StubResponse foundStubResponse;

    public PartialContentResponseHandlingStrategy(final StubResponse foundStubResponse) {
        this.foundStubResponse = foundStubResponse;
    }

    @Override
    public void handle(final HttpServletResponseWithGetStatus response, final StubRequest assertionStubRequest) throws IOException {
        HandlerUtils.setResponseMainHeaders(response);
        setStubResponseHeaders(foundStubResponse, response);

        simulateLatency();

        Range range = new Range(assertionStubRequest, foundStubResponse);

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Range", range.toHeader());

        FileChannel fileChannel = range.file.getChannel();

        byte[] buffer = new byte[8192];
        ByteBuffer wrappedBuffer = ByteBuffer.wrap(buffer);
        final OutputStream streamOut = response.getOutputStream();
        int n, bytesWritten = 0;
        while ((n = fileChannel.read(wrappedBuffer)) != -1 && bytesWritten < range.getContentSize()) {
            streamOut.write(buffer, 0, Math.min(wrappedBuffer.position(), (int) (range.getContentSize() - bytesWritten)));
            bytesWritten += n;
            wrappedBuffer.clear();
        }
        streamOut.flush();
        streamOut.close();
        fileChannel.close();
    }

    private void simulateLatency() {
        if (StringUtils.isSet(foundStubResponse.getLatency())) {
            try {
                final long latency = Long.parseLong(foundStubResponse.getLatency());
                TimeUnit.MILLISECONDS.sleep(latency);
            } catch (final InterruptedException e) {
                throw new Stubby4JException(e);
            }
        }
    }

    private void setStubResponseHeaders(final StubResponse stubResponse, final HttpServletResponse response) {
        response.setCharacterEncoding(StringUtils.UTF_8);
        for (Map.Entry<String, String> entry : stubResponse.getHeaders().entrySet()) {
            response.setHeader(entry.getKey(), entry.getValue());
        }
    }

    // http://tools.ietf.org/html/rfc2616#section-14.35.1
    private static class Range {
        private final static Pattern PATTERN = Pattern.compile("bytes=(\\d+)-(\\d+)?");

        private final long start;
        private final long end;
        private final long length;
        private final RandomAccessFile file;

        public Range(StubRequest request, StubResponse response) throws IOException {
            File file = response.getRawFile();
            if (file == null) {
                throw new IOException("no file set in response");
            }
            if (!file.canRead()) {
                throw new IOException("file " + file + " not readable");
            }
            Matcher matcher = PATTERN.matcher(getRange(request));
            if (matcher.matches()) {
                start = Long.parseLong(matcher.group(1));
                if (matcher.group(2) != null) {
                    end = Long.parseLong(matcher.group(2));
                } else {
                    end = file.length();
                }
            } else {
                throw new IOException("invalid range request");
            }
            this.file = new RandomAccessFile(file, "r");
            this.length = file.length();
        }



        public String toHeader() {
            return String.format("bytes %d-%d/%d", start, end, length);
        }

        public long getContentSize() {
            return end-start+1;
        }

        private static String getRange(StubRequest request) throws IOException {
            String range = request.getHeaders().get(StubRequest.RANGE_HEADER);
            if (!StringUtils.isSet(range)) {
                throw new IOException("No range header in request");
            }
            return range;
        }
    }
}
