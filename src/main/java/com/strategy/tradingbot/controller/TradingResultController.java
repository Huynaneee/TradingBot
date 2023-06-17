package com.strategy.tradingbot.controller;

import com.strategy.tradingbot.service.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class TradingResultController {

    private final ServiceImpl service;

    @GetMapping("/list")
    public String getListCurrentPriceCoin() {
        service.addCandlestickEvent();
        return "Поехали";
    }
}
