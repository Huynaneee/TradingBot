package com.strategy.tradingbot.service;

import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.event.CandlestickEvent;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.strategy.tradingbot.entity.Candle;
import com.strategy.tradingbot.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceImpl {
    private final BinanceApiWebSocketClient binanceApiWebSocketClient;
    private final CandleRepository candleRepository;

    public void addCandlestickEvent() {
        List<CandlestickEvent> candlestickEvents = new ArrayList<>();
        binanceApiWebSocketClient.onCandlestickEvent("btcusdt", CandlestickInterval.FIFTEEN_MINUTES, response -> {
            candlestickEvents.add(response);
            System.out.println(response);
            if (response.getBarFinal()) {
                candleRepository.save(processCandlestickEvents(candlestickEvents));
                candlestickEvents.clear();
                System.out.println("buuu");
            }
        });
    }

    public Candle processCandlestickEvents(List<CandlestickEvent> candlestickEvents) {
        CandlestickEvent firstEvent = candlestickEvents.stream().findFirst().orElseThrow();
        CandlestickEvent lastRecord = candlestickEvents.get(candlestickEvents.size() - 1);
        return Candle.builder()
                .dateTime(ZonedDateTime.now())
                .startBodyPrice(firstEvent.getOpen())
                .endBodyPrice(lastRecord.getClose())
                .highGround(firstEvent.getHigh())
                .lowGround(lastRecord.getLow())
                .colorIsGreen(compareBigIntegers(firstEvent.getOpen(), lastRecord.getClose()))
                .symbolName(firstEvent.getSymbol())
                .candlestickInterval(firstEvent.getIntervalId()).build();
    }
    public boolean compareBigIntegers(String firstValueStr, String secondValueStr) {
        return firstValueStr.compareTo(secondValueStr) < 0;
    }
}
