package ApplicationLayer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ApplicationLayer.Enums.ErrorType;

public class Response<T> {
    private final T data;
    private final String message;
    private final boolean success;
    private final ErrorType errorType;
    private final String token;

    public Response() {
        this.data = null;
        this.message = null;
        this.success = false;
        this.errorType = null;
        this.token = null;
    }
    

    @JsonCreator
    public Response(
        @JsonProperty("data") T data,
        @JsonProperty("message") String message,
        @JsonProperty("success") boolean success,
        @JsonProperty("errorType") ErrorType errorType,
        @JsonProperty("token") String token
    ) {
        this.data = data;
        this.message = message;
        if(!success && errorType == null){
            throw new IllegalArgumentException("Error type must be provided if success is false");
        }
        this.success = success;
        if(success) {
            this.errorType = null;
        } else {
            this.errorType = errorType;
        }
        this.token = token;
    }



    public String getToken(){
        return token;
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

    public ErrorType getErrorType() {
        return errorType;
    }

}
