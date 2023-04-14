package io.spaship.sidecar.type;

public class OperationException extends Exception {


    public OperationException() {
        super("something went wrong, this case was not handled, " +
                "please check the debug messages for more details");
    }

    public OperationException(String message) {
        super(message);
    }

    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
