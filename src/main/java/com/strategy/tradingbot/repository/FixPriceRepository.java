package com.strategy.tradingbot.repository;

import com.strategy.tradingbot.entity.Candle;
import com.strategy.tradingbot.entity.FixPriceFVG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixPriceRepository extends JpaRepository<FixPriceFVG, Long> {

    FixPriceFVG findByCandle(Candle candle);

    @Query("SELECT f FROM FixPriceFVG f JOIN FETCH f.priceBuyOrder WHERE f.unprocessed = true")
    List<FixPriceFVG> findByUnprocessedTrueWithPriceBuyOrder();

    @Query("SELECT f FROM FixPriceFVG f WHERE f.unprocessed = true AND f.isStopLossActive = false")
    List<FixPriceFVG> findByUnprocessedTrueAndStopLossNotActive();

}
