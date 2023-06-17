package com.strategy.tradingbot.config;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.impl.BinanceApiWebSocketClientImpl;
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

    private final BinanceApiClient binanceApiClient;

    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient();
    }

    @Bean
    public BinanceApiRestClient binanceApiRestClient() {
        return BinanceApiClientFactory.newInstance(binanceApiClient.getApiKey(), binanceApiClient.getSecretKey()).newRestClient();
    }

    @Bean
    public BinanceApiWebSocketClient binanceApiWebSocketClient() {
        return new BinanceApiWebSocketClientImpl(httpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
