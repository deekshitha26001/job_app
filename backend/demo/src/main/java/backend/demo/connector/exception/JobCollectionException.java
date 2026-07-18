package backend.demo.connector.exception;

public class JobCollectionException extends ConnectorException {
    public JobCollectionException(String message) {
        super(message);
    }

    public JobCollectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
