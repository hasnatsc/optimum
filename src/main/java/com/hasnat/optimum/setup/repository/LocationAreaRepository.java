package com.hasnat.optimum.setup.repository;
import com.hasnat.optimum.setup.entity.LocationArea;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface LocationAreaRepository extends JpaRepository<LocationArea, Long> {
    List<LocationArea> findByCityIdAndActiveTrueOrderByName(Long cityId);
}
