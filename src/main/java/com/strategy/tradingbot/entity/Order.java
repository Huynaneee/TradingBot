package com.strategy.tradingbot.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Order {

    private String priceBuy;

    private String priceSell;

    private String countBuy;

    private String resultOrder;

    private String stopLoss;

    private String takeProfit;

    private StatusOrder statusOrder;
}
