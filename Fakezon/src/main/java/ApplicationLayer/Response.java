package ApplicationLayer;

import ApplicationLayer.Enums.ErrorType;

public class Response<T> {
    private final T data;
    private final String message;
    private final boolean success;
    private final ErrorType errorType;

    public Response(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.errorType = null;
    }

    public Response(T data, String message, boolean success, ErrorType errorType) {
        this.data = data;
        this.message = message;
        this.success = success;
        if(success) {
            this.errorType = null;
        } else {
            this.errorType = errorType;
        }
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

}
