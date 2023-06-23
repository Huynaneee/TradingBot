package com.strategy.tradingbot.service;

import com.strategy.tradingbot.entity.Candle;
import com.strategy.tradingbot.entity.FixPriceFVG;
import com.strategy.tradingbot.repository.CandleRepository;
import com.strategy.tradingbot.repository.FixPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchFVGServiceImpl {
    private static final String FIB_05 = "fib05";
    private static final String FIB_618 = "fib0618";
    private static final String FIB_786 = "fib0786";
    private static final String FIB_1 = "fib1";
    private final CandleRepository candleRepository;
    private final FixPriceRepository fixPriceRepository;

    @Scheduled(fixedRate = 60000)
    public void searchFVG() {
        var candles = candleRepository.findByFVGTrue();
        if (candles.isEmpty()) {
            return;
        }

        for (Candle secondCandle : candles) {
            var firstCandle = candleRepository.findById(secondCandle.getId() - 1).orElseThrow();
            if (secondCandle.isColorIsGreen()) {
                var startPrice = new BigDecimal(secondCandle.getStartBodyPrice());
                var endPrice = new BigDecimal(secondCandle.getEndBodyPrice());
                BigDecimal fib0_5 = calculateFibonacciLevel(endPrice, startPrice, BigDecimal.valueOf(0.5));
                BigDecimal fib0_618 = calculateFibonacciLevel(endPrice, startPrice, BigDecimal.valueOf(0.618));
                BigDecimal fib0_786 = calculateFibonacciLevel(endPrice, startPrice, BigDecimal.valueOf(0.786));
                BigDecimal fib1 = calculateFibonacciLevel(endPrice, startPrice, BigDecimal.valueOf(1));
                Map<String, BigDecimal> valueFibonacci = new HashMap<>();
                valueFibonacci.put(FIB_05, fib0_5);
                valueFibonacci.put(FIB_618, fib0_618);
                valueFibonacci.put(FIB_786, fib0_786);
                valueFibonacci.put(FIB_1, fib1);
                List<Candle> candleDateAfter = candleRepository.findByOpenDateTimeAfter(secondCandle.getOpenDateTime());
                boolean areAllCandlesNotCrossed = candleDateAfter.stream().allMatch(candle ->
                        new BigDecimal(candle.getLowGround()).compareTo(fib0_5) >= 0
                );

                FixPriceFVG existingFixPrice = fixPriceRepository.findByCandle(secondCandle);
                if (existingFixPrice == null) {
                    fixPriceRepository.save(FixPriceFVG.builder()
                            .isOrder(false)
                            .isLong(true)
                            .unprocessed(areAllCandlesNotCrossed)
                            .priceBuyOrder(valueFibonacci)
                            .stopLoss(new BigDecimal(firstCandle.getLowGround()))
                            .candle(secondCandle)
                            .build());
                }
            } else {
                var endPrice = new BigDecimal(secondCandle.getStartBodyPrice());
                var startPrice = new BigDecimal(secondCandle.getEndBodyPrice());
                Map<String, BigDecimal> valueFibonacci = new HashMap<>();
                BigDecimal fib0_5 = calculateFibonacciLevel(startPrice, endPrice, BigDecimal.valueOf(0.5));
                BigDecimal fib0_618 = calculateFibonacciLevel(startPrice, endPrice, BigDecimal.valueOf(0.618));
                BigDecimal fib0_786 = calculateFibonacciLevel(startPrice, endPrice, BigDecimal.valueOf(0.786));
                BigDecimal fib1 = calculateFibonacciLevel(startPrice, endPrice, BigDecimal.valueOf(1));
                valueFibonacci.put(FIB_05, fib0_5);
                valueFibonacci.put(FIB_618, fib0_618);
                valueFibonacci.put(FIB_786, fib0_786);
                valueFibonacci.put(FIB_1, fib1);
                List<Candle> candleDateAfter = candleRepository.findByOpenDateTimeAfter(secondCandle.getOpenDateTime());
                boolean areAllCandlesNotCrossed = candleDateAfter.stream().allMatch(candle ->
                        new BigDecimal(candle.getLowGround()).compareTo(fib0_5) <= 0
                );

                FixPriceFVG existingFixPrice = fixPriceRepository.findByCandle(secondCandle);
                if (existingFixPrice == null) {
                    fixPriceRepository.save(FixPriceFVG.builder()
                            .isOrder(false)
                            .isLong(false)
                            .unprocessed(areAllCandlesNotCrossed)
                            .priceBuyOrder(valueFibonacci)
                            .stopLoss(new BigDecimal(firstCandle.getLowGround()))
                            .candle(secondCandle)
                            .build());
                }
            }
        }
    }

    public BigDecimal calculateFibonacciLevel(BigDecimal startPrice, BigDecimal endPrice, BigDecimal level) {
        BigDecimal diff = endPrice.subtract(startPrice);
        return startPrice.add(diff.multiply(level));
    }

    public BigDecimal calculateLongPercentageDown(Candle candle) {
        BigDecimal endPrice = new BigDecimal(candle.getLowGround());
        BigDecimal percentage = endPrice.multiply(BigDecimal.valueOf(0.001)); // 0.1% эквивалентно 0.001 в десятичном представлении
        return endPrice.subtract(percentage);
    }

    public BigDecimal calculateShortPercentageUp(Candle candle) {
        BigDecimal highGround = new BigDecimal(candle.getHighGround());
        BigDecimal percentage = highGround.multiply(BigDecimal.valueOf(0.001)); // 0.1% эквивалентно 0.001 в десятичном представлении
        return highGround.add(percentage);
    }
}
