package com.creditsuisse.crypto.order;

import com.creditsuisse.crypto.order.model.Order;
import com.creditsuisse.crypto.order.model.OrderInstruction;

import java.util.List;

public interface OrderRepository {

    boolean place(Order order);

    boolean cancel(String id);

    List<Order> filterBy(OrderInstruction instruction);

}
