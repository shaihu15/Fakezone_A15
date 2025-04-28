package ApplicationLayer;

public class Response<T> {
    private final T data;
    private final String message;
    private final boolean success;

    public Response(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
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
