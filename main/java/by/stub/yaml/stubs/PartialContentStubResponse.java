package by.stub.yaml.stubs;

public class PartialContentStubResponse extends StubResponse {
    public PartialContentStubResponse(StubResponse stubResponse) {
        super(stubResponse.getStatus(),
                stubResponse.getBody(),
                stubResponse.getRawFile(),
                stubResponse.getLatency(),
                stubResponse.getHeaders());
    }

    @Override
    public StubResponseTypes getStubResponseType() {
        return StubResponseTypes.PARTIAL_CONTENT_206;
    }
}
