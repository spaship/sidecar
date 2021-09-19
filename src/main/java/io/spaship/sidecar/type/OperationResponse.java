package io.spaship.sidecar.type;

public class OperationResponse {

    Environment environment;
    String sideCarServiceUrl;
    int status; //[-1] : restricted, [0] : skipped, [1] : created, [2] : modified, [3] : deleted
    String message;
    String errorMessage;
    Class<?> originatedFrom;

    OperationResponse(Environment environment, String sideCarServiceUrl, int status, String message, String errorMessage, Class originatedFrom) {
        this.environment = environment;
        this.sideCarServiceUrl = sideCarServiceUrl;
        this.status = status;
        this.message = message;
        this.errorMessage = errorMessage;
        this.originatedFrom = originatedFrom;
    }

    public static OperationResponseBuilder builder() {
        return new OperationResponseBuilder();
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public String getSideCarServiceUrl() {
        return this.sideCarServiceUrl;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Class<?> getOriginatedFrom() {
        return this.originatedFrom;
    }

    @Override
    public String toString() {
        return "{"
                + "\"environment\":" + environment
                + ", \"sideCarServiceUrl\":\"" + sideCarServiceUrl + "\""
                + ", \"status\":\"" + status + "\""
                + ", \"message\":\"" + message + "\""
                + ", \"errorMessage\":\"" + errorMessage + "\""
                + ", \"originatedFrom\":" + originatedFrom
                + "}";
    }

    public static class OperationResponseBuilder {
        private Environment environment;
        private String sideCarServiceUrl;
        private int status;
        private String message;
        private String errorMessage;
        private Class<?> originatedFrom;

        OperationResponseBuilder() {
        }

        public OperationResponseBuilder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public OperationResponseBuilder sideCarServiceUrl(String sideCarServiceUrl) {
            this.sideCarServiceUrl = sideCarServiceUrl;
            return this;
        }

        public OperationResponseBuilder status(int status) {
            this.status = status;
            switch (status) {
                case -1:
                    return message("restricted");
                case 0:
                    return message("skipped");
                case 1:
                    return message("created");
                case 2:
                    return message("modified");
                case 3:
                    return message("deleted");

                default:
                    throw new IllegalStateException("Unexpected value: " + status);
            }
        }

        public OperationResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public OperationResponseBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public OperationResponseBuilder originatedFrom(Class<?> originatedFrom) {
            this.originatedFrom = originatedFrom;
            return this;
        }

        public OperationResponse build() {
            return new OperationResponse(environment, sideCarServiceUrl, status, message, errorMessage, originatedFrom);
        }

        @Override
        public String toString() {
            return "{"
                    + "\"environment\":" + environment
                    + ", \"sideCarServiceUrl\":\"" + sideCarServiceUrl + "\""
                    + ", \"status\":\"" + status + "\""
                    + ", \"message\":\"" + message + "\""
                    + ", \"errorMessage\":\"" + errorMessage + "\""
                    + ", \"originatedFrom\":" + originatedFrom
                    + "}";
        }
    }
}
