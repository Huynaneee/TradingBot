package com.strategy.tradingbot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@ToString
public class Candle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbolName;
    private ZonedDateTime openDateTime;
    private ZonedDateTime closeDateTime;
    private String candlestickInterval;
    private String highGround;
    private String lowGround;
    private String startBodyPrice;
    private String endBodyPrice;
    private boolean colorIsGreen;
    @Builder.Default
    private boolean FVG = false;
}
