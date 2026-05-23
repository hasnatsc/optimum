package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationCountry;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationCountryRepository extends JpaRepository<LocationCountry, Long> {
    Optional<LocationCountry> findByIsoCode(String isoCode);
    Optional<LocationCountry> findByIsoCode2(String isoCode2);
    List<LocationCountry> findByActiveTrueOrderByName();
}
