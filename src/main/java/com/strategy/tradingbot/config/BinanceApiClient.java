package com.strategy.tradingbot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Getter
@RequiredArgsConstructor
public class BinanceApiClient {

    @Value("${binance.api-key}")
    private String apiKey;
    @Value("${binance.secret-key}")
    private String secretKey;

}
