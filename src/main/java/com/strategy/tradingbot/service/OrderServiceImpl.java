package com.strategy.tradingbot.service;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.strategy.tradingbot.entity.FixPriceFVG;
import com.strategy.tradingbot.entity.Order;
import com.strategy.tradingbot.entity.StatusOrder;
import com.strategy.tradingbot.repository.FixPriceRepository;
import com.strategy.tradingbot.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private static final String FIB_05 = "fib05";
    private static final String FIB_618 = "fib0618";
    private static final String FIB_786 = "fib0786";
    private static final String FIB_1 = "fib1";

    private final FixPriceRepository fixPriceRepository;
    private final UMFuturesClientImpl futuresClient;
    private final OrderRepository orderRepository;


    @Scheduled(fixedDelay = 100000)
    public void saveOrder() {
        var fixPriceList = fixPriceRepository.findByUnprocessedTrueWithPriceBuyOrder();
        for (FixPriceFVG fixPriceFVG : fixPriceList) {
            BigDecimal stopLoss = fixPriceFVG.getStopLoss();
            boolean aLong = fixPriceFVG.isLong();
            Map<String, BigDecimal> priceBuyOrder = fixPriceFVG.getPriceBuyOrder();
            for (Map.Entry<String, BigDecimal> entry : priceBuyOrder.entrySet()) {
                BigDecimal price = entry.getValue();
                if (orderRepository.findByPriceBuy(price).isEmpty()) {
                    var order = Order.builder()
                            .statusOrder(StatusOrder.NEW)
                            .stopLoss(stopLoss)
                            .fixPriceFVG(fixPriceFVG)
                            .build();
                    if (aLong) {
                        order.setPriceBuy(price);
                    } else {
                        order.setPriceSell(price);
                    }
                    orderRepository.save(order);
                }
            }
            fixPriceFVG.setOrder(true);
            fixPriceRepository.save(fixPriceFVG);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void sendOrder() {
        List<Order> allByStatusOrderNew = orderRepository.findAllByStatusOrder(StatusOrder.NEW);
        if (allByStatusOrderNew.isEmpty()) {
            return;
        }
        for (Order order : allByStatusOrderNew) {
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            if (order.getPriceBuy() != null) {
                var priceBuy = order.getPriceBuy();
                parameters.put("symbol", "BTCUSDT");
                parameters.put("side", "BUY");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", new BigDecimal("0.001"));
                BigDecimal tickSize = new BigDecimal("0.1"); // Пример значения шага изменения цены
                BigDecimal roundedPrice = priceBuy.divide(tickSize, 0, RoundingMode.DOWN).multiply(tickSize);
                parameters.put("price", roundedPrice);
//                parameters.put("stopPrice", order.getStopLoss());
                if (!order.isSent()) {
                    var response = futuresClient.account().newOrder(parameters);
                    order.setOrderId(getJSONIntValue(response, "orderId"));
                    order.setClientOrderId(getJSONStringValue(response, "clientOrderId"));
                    order.setSent(true);
                }
                order.setStatusOrder(StatusOrder.START);
                orderRepository.save(order);
            } else {
                var priceSell = order.getPriceSell();
                parameters.put("symbol", "BTCUSDT");
                parameters.put("side", "SELL");
                parameters.put("type", "LIMIT");
                parameters.put("timeInForce", "GTC");
                parameters.put("quantity", new BigDecimal("0.001"));
                BigDecimal tickSize = new BigDecimal("0.1"); // Пример значения шага изменения цены
                BigDecimal roundedPrice = priceSell.divide(tickSize, 0, RoundingMode.DOWN).multiply(tickSize);
                parameters.put("price", roundedPrice);
                parameters.put("stopPrice", order.getStopLoss());
//                parameters.put("stopPrice", order.getStopLoss());

                if (!order.isSent()) {
                    var response = futuresClient.account().newOrder(parameters);
                    order.setOrderId(getJSONIntValue(response, "orderId"));
                    order.setClientOrderId(getJSONStringValue(response, "clientOrderId"));
                    order.setSent(true);
                }
                order.setStatusOrder(StatusOrder.START);
                orderRepository.save(order);
            }
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void addStopLoss() {
        var fvgList = fixPriceRepository.findByUnprocessedTrueAndStopLossNotActive();
        if(fvgList.isEmpty()) {
            return;
        }

        for (FixPriceFVG candle: fvgList) {
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", "BTCUSDT");
            parameters.put("side", "SELL");
            parameters.put("type", "STOP_MARKET");
            parameters.put("stopPrice", candle.getStopLoss());
            parameters.put("closePosition", true);
            futuresClient.account().newOrder(parameters);
            candle.setStopLossActive(true);
            fixPriceRepository.saveAndFlush(candle);
        }
    }

    public static String getJSONStringValue(String json, String key) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getString(key);
        } catch (JSONException e) {
            throw new JSONException(String.format("[JSONParser] Failed to get \"%s\"  from JSON object", key));
        }
    }

    public static long getJSONIntValue(String json, String key) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getLong(key);
        } catch (JSONException e) {
            throw new JSONException(String.format("[JSONParser] Failed to get \"%s\" from JSON object", key));
        }
    }
}