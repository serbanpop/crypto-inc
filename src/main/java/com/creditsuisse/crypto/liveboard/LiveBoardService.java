package com.creditsuisse.crypto.liveboard;

import com.creditsuisse.crypto.order.OrderRepository;
import com.creditsuisse.crypto.order.model.Order;
import com.creditsuisse.crypto.order.model.OrderInstruction;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static com.creditsuisse.crypto.order.model.OrderInstruction.BUY;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class LiveBoardService {

    private final OrderRepository repository;

    public Map<BigDecimal, BigDecimal> liveBoard(OrderInstruction instruction, int limit) {
        return repository.filterBy(instruction)
                .stream()
                .sorted((o1, o2) -> liveBoardEntryComparator(instruction).compare(o1.getPricePerCoin(), o2.getPricePerCoin()))
                // assuming the top X refers to the number of orders, not entries on the live board
                .limit(limit)
                // the entries are grouped by the price/coin rather than the coin type
                .collect(toMap(Order::getPricePerCoin, Order::getQuantity, BigDecimal::add, () -> new TreeMap<>(liveBoardEntryComparator(instruction))));
    }

    private static Comparator<BigDecimal> liveBoardEntryComparator(OrderInstruction instruction) {
        return (o1, o2) -> o1.compareTo(o2) * (BUY.equals(instruction) ? -1 : 1);
    }

}
