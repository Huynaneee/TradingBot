package com.strategy.tradingbot.repository;

import com.strategy.tradingbot.entity.FixPriceFVG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixPriceRepository extends JpaRepository<FixPriceFVG, Long> {
}
