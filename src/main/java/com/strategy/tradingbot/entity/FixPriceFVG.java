package com.strategy.tradingbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "fix_price_fvg")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixPriceFVG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "candle_id")
    private Candle candle;

    @Column(name = "is_order")
    private boolean isOrder;

    @Column(name = "simple_stop_loss")
    private BigDecimal stopLoss;

    @Column(name = "is_long")
    private boolean isLong;

    @Column(name = "count_running_order")
    @Builder.Default
    private int countRunningOrder = 0;

    @ElementCollection
    private Map<String, BigDecimal> priceBuyOrder;

    @Builder.Default
    private boolean unprocessed = false;

    @Builder.Default
    private boolean isStopLossActive = false;
}