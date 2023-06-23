package com.strategy.tradingbot.config;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.impl.BinanceApiWebSocketClientImpl;
import com.binance.connector.futures.client.impl.CMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;


@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class BinanceClientConfig {

    public static final String UM_BASE_URL = "https://fapi.binance.com";

    private final BinanceApiClient binanceApiClient;
    @Bean
    public BinanceApiRestClient binanceApiRestClient() {
        return BinanceApiClientFactory.newInstance(binanceApiClient.getApiKey(), binanceApiClient.getSecretKey()).newRestClient();
    }

    @Bean
    public UMFuturesClientImpl client() {
        return new UMFuturesClientImpl(binanceApiClient.getApiKey(), binanceApiClient.getSecretKey(), UM_BASE_URL);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
