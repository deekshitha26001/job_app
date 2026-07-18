package backend.demo.connector.exception;

public class ConnectorTimeoutException extends ConnectorException {
    public ConnectorTimeoutException(String message) {
        super(message);
    }

    public ConnectorTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
