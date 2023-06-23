package com.strategy.tradingbot.repository;

import com.strategy.tradingbot.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, Long> {
    List<Candle> findByFVGTrue();
    List<Candle> findByOpenDateTimeAfter(ZonedDateTime fromDate);
    Candle findByOpenDateTime(ZonedDateTime openDateTime);
    List<Candle> findTop3ByOrderByIdDesc();
}
