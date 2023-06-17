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
import java.util.List;

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
    private String stopLoss;

    @Column(name = "is_long")
    private boolean isLong;

    @ElementCollection
    private List<BigDecimal> priceBuyOrder;

}