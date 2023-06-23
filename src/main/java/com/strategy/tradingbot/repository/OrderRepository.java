package com.strategy.tradingbot.repository;

import com.strategy.tradingbot.entity.Order;
import com.strategy.tradingbot.entity.StatusOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByPriceBuy(BigDecimal price);
    @EntityGraph(attributePaths = "fixPriceFVG")
    List<Order> findAllByStatusOrder(StatusOrder statusOrder);
}
