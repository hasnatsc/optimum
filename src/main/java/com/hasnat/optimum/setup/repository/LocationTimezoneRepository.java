package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationTimezone;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationTimezoneRepository extends JpaRepository<LocationTimezone, Long> {
    Optional<LocationTimezone> findByZoneId(String zoneId);
    List<LocationTimezone> findByActiveTrueOrderByZoneId();
}
