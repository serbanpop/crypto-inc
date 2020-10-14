package com.creditsuisse.crypto.liveboard;

import com.creditsuisse.crypto.order.model.Order;
import com.creditsuisse.crypto.order.model.OrderInstruction;
import com.creditsuisse.crypto.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static com.creditsuisse.crypto.order.model.CoinType.ETH;
import static com.creditsuisse.crypto.order.model.OrderInstruction.BUY;
import static com.creditsuisse.crypto.order.model.OrderInstruction.SELL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

class LiveBoardServiceTest {

    private static final int ARBITRARY_LARGE_LIMIT = 10;

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final LiveBoardService testInstance = new LiveBoardService(orderRepository);

    @BeforeEach
    void setUp() {
        reset(orderRepository);
    }

    @Test
    void mergesOrdersWithSamePrice() {
        given(orderRepository.filterBy(eq(SELL))).willReturn(asList(
                orderWith(SELL, 13.6, 350.1),
                orderWith(SELL, 14.53, 50.5),
                orderWith(SELL, 24.32, 186D),
                orderWith(SELL, 13.6, 3.5),
                orderWith(SELL, 14.53, 23.21))
        );

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(SELL, ARBITRARY_LARGE_LIMIT);

        assertThat(liveBoard).containsExactly(
                entry(BigDecimal.valueOf(13.6), BigDecimal.valueOf(353.6)),
                entry(BigDecimal.valueOf(14.53), BigDecimal.valueOf(73.71)),
                entry(BigDecimal.valueOf(24.32), BigDecimal.valueOf(186D))
        );
    }

    @Test
    void limitsNumberOfOrdersAfterOrdering() {
        given(orderRepository.filterBy(eq(SELL))).willReturn(asList(
                orderWith(SELL, 15.6, 350.1),
                orderWith(SELL, 14D, 50.5),
                orderWith(SELL, 13.9, 441.8),
                orderWith(SELL, 16.6, 3.5))
        );

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(SELL, 2);

        assertThat(liveBoard).containsExactly(
                entry(BigDecimal.valueOf(13.9), BigDecimal.valueOf(441.8)),
                entry(BigDecimal.valueOf(14D), BigDecimal.valueOf(50.5))
        );
    }

    @Test
    void limitsNumberOfOrdersBeforeMerging() {
        given(orderRepository.filterBy(eq(SELL))).willReturn(asList(
                orderWith(SELL, 13.6, 350.1),
                orderWith(SELL, 14D, 50.5),
                orderWith(SELL, 13.9, 441.8),
                orderWith(SELL, 13.6, 3.5))
        );

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(SELL, 2);

        assertThat(liveBoard).containsExactly(
                entry(BigDecimal.valueOf(13.6), BigDecimal.valueOf(353.6))
        );
    }

    @Test
    void sortsSellEntriesByPricePerCoinInAscendingOrder() {
        given(orderRepository.filterBy(eq(SELL))).willReturn(asList(
                orderWith(SELL, 13.6, 350.1),
                orderWith(SELL, 14D, 50.5),
                orderWith(SELL, 13.9, 441.8),
                orderWith(SELL, 13.6, 3.5))
        );

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(SELL, 10);

        assertThat(liveBoard).containsExactly(
                entry(BigDecimal.valueOf(13.6), BigDecimal.valueOf(353.6)),
                entry(BigDecimal.valueOf(13.9), BigDecimal.valueOf(441.8)),
                entry(BigDecimal.valueOf(14.0), BigDecimal.valueOf(50.5))
        );
    }

    @Test
    void sortsBuyEntriesByPricePerCoinInDescendingOrder() {
        given(orderRepository.filterBy(eq(BUY))).willReturn(asList(
                orderWith(BUY, 13.6, 350.1),
                orderWith(BUY, 14D, 50.5),
                orderWith(BUY, 13.9, 441.8),
                orderWith(BUY, 13.6, 3.5))
        );

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(BUY, 10);

        assertThat(liveBoard).containsExactly(
                entry(BigDecimal.valueOf(14.0), BigDecimal.valueOf(50.5)),
                entry(BigDecimal.valueOf(13.9), BigDecimal.valueOf(441.8)),
                entry(BigDecimal.valueOf(13.6), BigDecimal.valueOf(353.6))
        );
    }

    @Test
    void filtersEntriesByInstruction() {
        given(orderRepository.filterBy(eq(BUY))).willReturn(emptyList());

        testInstance.liveBoard(BUY, 10);

        then(orderRepository).should().filterBy(eq(BUY));
    }

    @Test
    void returnsEmptyBoardWhenRepositoryReturnsEmptyList() {
        given(orderRepository.filterBy(eq(BUY))).willReturn(emptyList());

        final Map<BigDecimal, BigDecimal> liveBoard = testInstance.liveBoard(BUY, 10);

        assertThat(liveBoard).isEmpty();
    }

    private static Order orderWith(OrderInstruction instruction, Double pricePerCoin, Double quantity) {
        return Order.builder()
                .userId("user1")
                .type(ETH)
                .instruction(instruction)
                .pricePerCoin(BigDecimal.valueOf(pricePerCoin))
                .quantity(BigDecimal.valueOf(quantity))
                .build();
    }

}