/**
 * Stand-in for Spring's {@code ResponseStatusException}: an unchecked
 * exception that carries the HTTP status the framework should map it to.
 */
public class ResponseStatusException extends RuntimeException {

    private final int status;

    public ResponseStatusException(int status, String reason) {
        super(reason);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
