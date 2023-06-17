package com.strategy.tradingbot.service;

import com.strategy.tradingbot.entity.Candle;
import com.strategy.tradingbot.entity.FixPriceFVG;
import com.strategy.tradingbot.repository.CandleRepository;
import com.strategy.tradingbot.repository.FixPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFVGServiceImpl {

    private final CandleRepository candleRepository;
    private final FixPriceRepository fixPriceRepository;

    @Scheduled(cron = "0 */1 * * * *")
    public void searchFVG() {
        var candles = candleRepository.findTop3ByOrderByDateTimeDesc();
        System.out.println(candles);
        System.out.println("Я начал работу босс!");
        if (candles.isEmpty()) {
            return;
        }
        if (candles.size() == 3) {
            var firstCandle = candles.get(2);
            var secondCandle = candles.get(1);
            var thirdCandle = candles.get(0);
            // Проверяем лонговый имбаланс
            if (secondCandle.isColorIsGreen()) {
                var highGroundFirst = new BigDecimal(firstCandle.getHighGround());
                var lowGroundThird = new BigDecimal(thirdCandle.getLowGround());
                // Высота первой свечи должна быть меньше низа третьей свечи
                if (highGroundFirst.compareTo(lowGroundThird) < 0) {
                    BigDecimal priceChange = lowGroundThird.subtract(highGroundFirst);
                    BigDecimal percentChange = priceChange.divide(highGroundFirst, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    // Проверяем что имбаланс больше 0.50% движения цены
                    if (percentChange.compareTo(new BigDecimal("0.50")) >= 0) {
                        List<BigDecimal> priceBuyOrder = new ArrayList<>();
                        var startPrice = new BigDecimal(secondCandle.getStartBodyPrice());
                        var endPrice = new BigDecimal(secondCandle.getEndBodyPrice());
                        BigDecimal fib0_5 = calculateFibonacciLevelLong(startPrice, endPrice, BigDecimal.valueOf(0.5));
                        BigDecimal fib0_618 = calculateFibonacciLevelLong(startPrice, endPrice, BigDecimal.valueOf(0.618));
                        BigDecimal fib0_786 = calculateFibonacciLevelLong(startPrice, endPrice, BigDecimal.valueOf(0.786));
                        BigDecimal fib1 = calculateFibonacciLevelLong(startPrice, endPrice, BigDecimal.valueOf(1));

                        var stopLoss = calculateLongPercentageDown(firstCandle); // 0.1% эквивалентно 0.001 в десятичном представлении
                        priceBuyOrder.add(fib0_5);
                        priceBuyOrder.add(fib0_618);
                        priceBuyOrder.add(fib0_786);
                        priceBuyOrder.add(fib1);
                        fixPriceRepository.save(FixPriceFVG.builder()
                                .candle(secondCandle)
                                .isOrder(false)
                                .isLong(true)
                                .stopLoss(String.valueOf(stopLoss))
                                .priceBuyOrder(priceBuyOrder)
                                .build());
                    }
                }
            }

            if (!secondCandle.isColorIsGreen()) {
                BigDecimal highGroundThird = new BigDecimal(thirdCandle.getHighGround());
                BigDecimal lowGroundFirst = new BigDecimal(firstCandle.getLowGround());
                // Низ первой свечи должен быть больше высоты третьей свечи
                if (lowGroundFirst.compareTo(highGroundThird) > 0) {
                    BigDecimal priceChange = lowGroundFirst.subtract(highGroundThird);
                    BigDecimal percentChange = priceChange.divide(lowGroundFirst, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    // Проверяем, что имбаланс больше 0.50% движения цены
                    if (percentChange.compareTo(new BigDecimal("0.50")) >= 0) {
                        List<BigDecimal> priceSellOrder = new ArrayList<>();
                        BigDecimal startPrice = new BigDecimal(secondCandle.getStartBodyPrice());
                        BigDecimal endPrice = new BigDecimal(secondCandle.getEndBodyPrice());
                        BigDecimal fib0_5 = calculateFibonacciLevelShort(startPrice, endPrice, BigDecimal.valueOf(0.5));
                        BigDecimal fib0_618 = calculateFibonacciLevelShort(startPrice, endPrice, BigDecimal.valueOf(0.618));
                        BigDecimal fib0_786 = calculateFibonacciLevelShort(startPrice, endPrice, BigDecimal.valueOf(0.786));
                        BigDecimal fib1 = calculateFibonacciLevelShort(startPrice, endPrice, BigDecimal.valueOf(1));
                        priceSellOrder.add(fib0_5);
                        priceSellOrder.add(fib0_618);
                        priceSellOrder.add(fib0_786);
                        priceSellOrder.add(fib1);

                        var stopLoss = calculateShortPercentageUp(thirdCandle);
                        fixPriceRepository.save(FixPriceFVG.builder()
                                .candle(secondCandle)
                                .isOrder(false)
                                .isLong(false)
                                .stopLoss(String.valueOf(stopLoss))
                                .priceBuyOrder(priceSellOrder)
                                .build());
                    }
                }
            }
        }
    }
    private BigDecimal calculateFibonacciLevelShort(BigDecimal start, BigDecimal end, BigDecimal level) {
        BigDecimal range = start.subtract(end); // Изменяем порядок вычитания
        BigDecimal fibonacciRange = range.multiply(level);
        return start.subtract(fibonacciRange);
    }
    private BigDecimal calculateFibonacciLevelLong(BigDecimal start, BigDecimal end, BigDecimal level) {
        BigDecimal range = end.subtract(start);
        BigDecimal fibonacciRange = range.multiply(level);
        return start.add(fibonacciRange);
    }

    public BigDecimal calculateLongPercentageDown(Candle candle) {
        BigDecimal endPrice = new BigDecimal(candle.getStartBodyPrice());
        BigDecimal percentage = endPrice.multiply(BigDecimal.valueOf(0.001)); // 0.1% эквивалентно 0.001 в десятичном представлении
        return endPrice.subtract(percentage);
    }

    public BigDecimal calculateShortPercentageUp(Candle candle) {
        BigDecimal startPrice = new BigDecimal(candle.getEndBodyPrice());
        BigDecimal percentage = startPrice.multiply(BigDecimal.valueOf(0.001)); // 0.1% эквивалентно 0.001 в десятичном представлении
        return startPrice.add(percentage);
    }

}
