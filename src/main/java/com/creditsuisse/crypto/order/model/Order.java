package com.creditsuisse.crypto.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class Order {

    @EqualsAndHashCode.Include
    private String id;
    private String userId;
    private OrderInstruction instruction;
    private CoinType type;
    private BigDecimal quantity;
    private BigDecimal pricePerCoin;

}
