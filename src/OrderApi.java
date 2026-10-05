import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderApi {

    private final OrderService service;

    public OrderApi(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDto> listOrders() {
        return service.listAll().stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable("id") String id) {
        return service.findById(id)
                .map(OrderDto::from)
                .orElseThrow(() -> new ResponseStatusException(404, "order not found: " + id));
    }

    /**
     * Create a new order for a customer.
     *
     * <p>The order is stored under a server-generated identifier and starts
     * in the {@code NEW} status. Responds with HTTP 201 Created on success.
     *
     * @param request the order to create; must be non-null, must carry a
     *                non-blank {@code customerId}, and must carry a non-null
     *                {@code amount} of zero or greater
     * @return an {@link OrderDto} describing the newly created order: its
     *         generated id, the customer id, the amount, and the status
     * @throws ResponseStatusException with status 400 if {@code request} is
     *         null, if {@code customerId} is null or blank, or if
     *         {@code amount} is null or negative
     */
    @PostMapping
    @ResponseStatus(201)
    public OrderDto createOrder(@RequestBody CreateOrderRequest request) {
        if (request == null || request.customerId() == null || request.customerId().isBlank()) {
            throw new ResponseStatusException(400, "customerId is required");
        }
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() < 0) {
            throw new ResponseStatusException(400, "amount must be >= 0");
        }
        return OrderDto.from(service.create(request.customerId(), amount));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(204)
    public void cancelOrder(@PathVariable("id") String id) {
        if (!service.cancel(id)) {
            throw new ResponseStatusException(409, "order is unknown or already cancelled: " + id);
        }
    }
}
