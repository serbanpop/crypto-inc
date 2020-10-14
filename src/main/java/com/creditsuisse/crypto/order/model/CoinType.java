package com.creditsuisse.crypto.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoinType {

    BTC("Bitcoin"),
    ETH("Ethereum"),
    LTC("Litecoin"),
    XRP("Ripple");

    String name;

}
