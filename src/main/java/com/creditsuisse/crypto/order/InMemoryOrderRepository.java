package com.creditsuisse.crypto.order;

import com.creditsuisse.crypto.order.model.Order;
import com.creditsuisse.crypto.order.model.OrderInstruction;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class InMemoryOrderRepository implements OrderRepository {

    // chose to store all orders rather than aggregating them, as orders should be cancellable
    private List<Order> orders = new ArrayList<>();

    @Override
    public boolean place(Order toAdd) {
        return orders.stream().noneMatch(order -> order.getId().equals(toAdd.getId())) && orders.add(toAdd);
    }

    @Override
    public boolean cancel(String orderId) {
        return orders.removeIf(order -> order.getId().equals(orderId));
    }

    // this repository doesn't perform any transformations/aggregations as it should be easily replaced with a DB if needed
    @Override
    public List<Order> filterBy(OrderInstruction instruction) {
        return orders.stream()
                .filter(order -> order.getInstruction().equals(instruction))
                .collect(toList());
    }

    List<Order> all() {
        return orders;
    }
}
