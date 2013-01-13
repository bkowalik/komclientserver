package common.exceptions;


public class ClinetException extends Exception {
    public ClinetException() {
    }

    public ClinetException(String message) {
        super(message);
    }

    public ClinetException(String message, Throwable cause) {
        super(message, cause);
    }
}
