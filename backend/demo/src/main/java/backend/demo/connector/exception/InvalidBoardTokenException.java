package backend.demo.connector.exception;

public class InvalidBoardTokenException extends ConnectorException {
    public InvalidBoardTokenException(String message) {
        super(message);
    }
}
