package com.strategy.tradingbot.repository;

import com.strategy.tradingbot.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, Long> {

    List<Candle> findTop3ByOrderByDateTimeDesc();
}
