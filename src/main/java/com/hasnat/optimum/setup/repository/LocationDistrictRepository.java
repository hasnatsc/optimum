package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationDistrict;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationDistrictRepository extends JpaRepository<LocationDistrict, Long> {
    List<LocationDistrict> findByStateIdAndActiveTrueOrderByName(Long stateId);
    Optional<LocationDistrict> findByStateIdAndCode(Long stateId, String code);
}
