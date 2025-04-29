package ApplicationLayer;

public class Request<T> {
    private final String token;
    private final T data;

    public Request(String token, T data) {
        this.token = token;
        this.data = data;
    }

    public String getToken() {
        return token;
    }

    public T getData() {
        return data;
    }
}
