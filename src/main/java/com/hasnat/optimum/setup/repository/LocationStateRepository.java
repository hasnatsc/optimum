package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationState;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationStateRepository extends JpaRepository<LocationState, Long> {
    List<LocationState> findByCountryIdAndActiveTrueOrderByName(Long countryId);
    Optional<LocationState> findByCountryIdAndCode(Long countryId, String code);
}
