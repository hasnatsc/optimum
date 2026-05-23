package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationCity;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationCityRepository extends JpaRepository<LocationCity, Long> {
    List<LocationCity> findByDistrictIdAndActiveTrueOrderByName(Long districtId);
}
