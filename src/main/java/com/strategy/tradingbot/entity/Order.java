package com.strategy.tradingbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_by_FVG")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientOrderId;

    private Long orderId;

    private BigDecimal priceBuy;

    private BigDecimal priceSell;

    private BigDecimal countBuy;

    @ManyToOne
    @JoinColumn(name = "fix_price_id")
    private FixPriceFVG fixPriceFVG;

    private BigDecimal stopLoss;

    private BigDecimal takeProfit;

    @Builder.Default
    private boolean isSent = false;

    @Enumerated(EnumType.STRING)
    private StatusOrder statusOrder;

}
