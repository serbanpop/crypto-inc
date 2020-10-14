package com.creditsuisse.crypto.order;

import com.creditsuisse.crypto.order.InMemoryOrderRepository;
import com.creditsuisse.crypto.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.creditsuisse.crypto.order.model.CoinType.ETH;
import static com.creditsuisse.crypto.order.model.OrderInstruction.BUY;
import static com.creditsuisse.crypto.order.model.OrderInstruction.SELL;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOrderRepositoryTest {

    private static final String SELL_ORDER_ID_1 = UUID.randomUUID().toString();
    private static final String SELL_ORDER_ID_2 = UUID.randomUUID().toString();
    private static final String USER_ID = "john.doe";

    private static final Order BUY_ORDER = Order.builder()
            .id(UUID.randomUUID().toString())
            .userId(USER_ID)
            .instruction(BUY)
            .type(ETH)
            .quantity(BigDecimal.valueOf(45))
            .pricePerCoin(TEN)
            .build();

    private static final Order SELL_ORDER_1 = Order.builder()
            .id(SELL_ORDER_ID_1)
            .userId(USER_ID)
            .instruction(SELL)
            .type(ETH)
            .quantity(TEN)
            .pricePerCoin(ONE)
            .build();

    private static final Order SELL_ORDER_2 = Order.builder()
            .id(SELL_ORDER_ID_2)
            .userId(USER_ID)
            .instruction(SELL)
            .type(ETH)
            .quantity(BigDecimal.valueOf(30))
            .pricePerCoin(ONE)
            .build();

    private InMemoryOrderRepository testInstance;

    @BeforeEach
    void setUp() {
        testInstance = new InMemoryOrderRepository();
    }

    @Test
    void shouldPlaceOrder() {
        assertThat(testInstance.all()).isEmpty();

        boolean placed = testInstance.place(SELL_ORDER_1);

        assertThat(placed).isTrue();
        assertThat(testInstance.all()).containsOnly(SELL_ORDER_1);
    }

    @Test
    void shouldSkipOrderWithDuplicateId() {
        givenRepositoryContains(SELL_ORDER_1);
        final Order arbitraryOrderWithSameId = Order.builder()
                .id(SELL_ORDER_ID_1)
                .build();

        boolean placed = testInstance.place(arbitraryOrderWithSameId);

        assertThat(placed).isFalse();
        assertThat(testInstance.all()).containsOnly(SELL_ORDER_1);
    }


    @Test
    void shouldCancelOrder() {
        givenRepositoryContains(SELL_ORDER_1, SELL_ORDER_2);

        boolean cancelled = testInstance.cancel(SELL_ORDER_ID_1);

        assertThat(cancelled).isTrue();
        assertThat(testInstance.all()).containsOnly(SELL_ORDER_2);
    }

    @Test
    void shouldIgnoreOrderIdWhenOrderDoesNotExist() {
        givenRepositoryContains(SELL_ORDER_1);

        boolean cancelled = testInstance.cancel(SELL_ORDER_ID_2);

        assertThat(cancelled).isFalse();
        assertThat(testInstance.all()).containsOnly(SELL_ORDER_1);
    }

    @Test
    void filtersOrdersByInstruction() {
        givenRepositoryContains(BUY_ORDER, SELL_ORDER_1, SELL_ORDER_2);

        List<Order> sellOrders = testInstance.filterBy(SELL);

        assertThat(sellOrders).allMatch(order -> order.getInstruction().equals(SELL));
    }

    @Test
    void filterByInstructionReturnsEmptyList_whenNoOrdersMatchInstruction() {
        givenRepositoryContains(SELL_ORDER_1, SELL_ORDER_2);

        List<Order> sellOrders = testInstance.filterBy(BUY);

        assertThat(sellOrders).isEmpty();
    }

    @Test
    void filterByInstructionReturnsEmptyList_whenRepositoryContainsNoOrders() {
        assertThat(testInstance.all()).isEmpty();

        List<Order> sellOrders = testInstance.filterBy(BUY);

        assertThat(sellOrders).isEmpty();

    }

    private void givenRepositoryContains(Order... orders) {
        stream(orders).forEach(testInstance::place);
        assertThat(testInstance.all()).containsOnlyOnceElementsOf(asList(orders));
    }
}