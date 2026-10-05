import java.math.BigDecimal;

/** Request body for {@code POST /orders}. */
public record CreateOrderRequest(String customerId, BigDecimal amount) {
}
