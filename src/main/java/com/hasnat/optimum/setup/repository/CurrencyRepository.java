package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.Currency;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);
    List<Currency> findByActiveTrueOrderByCode();
    boolean existsByCode(String code);
}
