package com.hasnat.optimum.global.repository;
import com.hasnat.optimum.global.entity.BusinessDocumentLineLot;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface BusinessDocumentLineLotRepository extends JpaRepository<BusinessDocumentLineLot, Long> {
    List<BusinessDocumentLineLot> findByDocumentLineId(Long lineId);
    List<BusinessDocumentLineLot> findByLotId(Long lotId);
    void deleteByDocumentLineId(Long lineId);
}
