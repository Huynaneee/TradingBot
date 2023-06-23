package com.strategy.tradingbot.service;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.strategy.tradingbot.entity.Candle;
import com.strategy.tradingbot.repository.CandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CandleServiceImpl {

    private final CandleRepository candleRepository;

    private final BinanceApiRestClient binanceApiRestClient;

    @PostConstruct
    public void getListCandlestick() {
        List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars("BTCUSDT", CandlestickInterval.FIFTEEN_MINUTES);
        candlesticks.remove(candlesticks.size() - 1);
        List<Candle> candleList = candlesticks.stream().map(s -> Candle.builder()
                        .openDateTime(getZonedDateTimeFromMilliseconds(s.getOpenTime()))
                        .closeDateTime(getZonedDateTimeFromMilliseconds(s.getCloseTime()))
                        .candlestickInterval(CandlestickInterval.FIFTEEN_MINUTES.name())
                        .symbolName("BTCUSDT")
                        .highGround(s.getHigh())
                        .lowGround(s.getLow())
                        .startBodyPrice(s.getOpen())
                        .endBodyPrice(s.getClose())
                        .colorIsGreen(compareBigIntegers(s.getOpen(), s.getClose()))
                        .build())
                .sorted(Comparator.comparing(Candle::getOpenDateTime))
                .toList();
        for (int i = 1; i < candleList.size() - 1; i++) {
            Candle secondCandle = candleList.get(i);
            Candle firstCandle = candleList.get(i - 1);
            Candle thirdCandle = candleList.get(i + 1);
            if (secondCandle.isColorIsGreen()) {
                var firstCandleHighGround = new BigDecimal(firstCandle.getHighGround());
                var thirdCandleLowGround = new BigDecimal(thirdCandle.getLowGround());
                if (firstCandleHighGround.compareTo(thirdCandleLowGround.multiply(new BigDecimal("0.995"))) < 0) {
                    secondCandle.setFVG(true);
                }
            } else {
                var firstCandleLowGround = new BigDecimal(firstCandle.getLowGround());
                var thirdCandleHighGround = new BigDecimal(thirdCandle.getHighGround());
                if (firstCandleLowGround.compareTo(thirdCandleHighGround.multiply(new BigDecimal("1.005"))) > 0) {
                    secondCandle.setFVG(true);
                }
            }
        }
        candleRepository.saveAll(candleList);
    }

    private ZonedDateTime getZonedDateTimeFromMilliseconds(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public boolean compareBigIntegers(String firstValueStr, String secondValueStr) {
        return firstValueStr.compareTo(secondValueStr) < 0;
    }

    @Scheduled(fixedDelay = 60000)
    public void getNewCandle() {
        List<Candlestick> candlesticks = binanceApiRestClient.getCandlestickBars("BTCUSDT", CandlestickInterval.FIFTEEN_MINUTES, 2, null, null);
        candlesticks.remove(candlesticks.size() - 1);
        Candlestick candle = candlesticks.get(0);
        ZonedDateTime dateTimeFromMilliseconds = getZonedDateTimeFromMilliseconds(candle.getOpenTime());
        Candle candleByOpenDateTime = candleRepository.findByOpenDateTime(dateTimeFromMilliseconds);
        if (candleByOpenDateTime == null) {
            candleRepository.save(Candle.builder()
                    .openDateTime(dateTimeFromMilliseconds)
                    .closeDateTime(getZonedDateTimeFromMilliseconds(candle.getCloseTime()))
                    .endBodyPrice(candle.getClose())
                    .startBodyPrice(candle.getClose())
                    .lowGround(candle.getLow())
                    .highGround(candle.getHigh())
                    .symbolName("BTCUSDT")
                    .candlestickInterval(CandlestickInterval.FIFTEEN_MINUTES.name())
                    .colorIsGreen(compareBigIntegers(candle.getOpen(), candle.getClose()))
                    .build());
        }
    }

    @Scheduled(fixedDelay = 100000)
    public void searchFVG() {
        List<Candle> top3Candle = candleRepository.findTop3ByOrderByIdDesc();
        Candle firstCandle = top3Candle.get(2);
        Candle secondCandle = top3Candle.get(1);
        Candle thirdCandle = top3Candle.get(0);

        if (secondCandle.isColorIsGreen()) {
            var firstCandleHighGround = new BigDecimal(firstCandle.getHighGround());
            var thirdCandleLowGround = new BigDecimal(thirdCandle.getLowGround());
            if (firstCandleHighGround.compareTo(thirdCandleLowGround.multiply(new BigDecimal("0.995"))) < 0) {
                secondCandle.setFVG(true);
                candleRepository.save(secondCandle);
            }
        } else {
            var firstCandleLowGround = new BigDecimal(firstCandle.getLowGround());
            var thirdCandleHighGround = new BigDecimal(thirdCandle.getHighGround());
            if (firstCandleLowGround.compareTo(thirdCandleHighGround.multiply(new BigDecimal("1.005"))) > 0) {
                secondCandle.setFVG(true);
                candleRepository.save(secondCandle);
            }
        }
    }

    public void checkOrder(){

    }
}
