package com.hasnat.optimum.commercial.repository;
import com.hasnat.optimum.commercial.entity.CommercialInvoiceItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface CommercialInvoiceItemRepository extends JpaRepository<CommercialInvoiceItem, Long> {
    List<CommercialInvoiceItem> findByInvoiceId(Long invoiceId);
    List<CommercialInvoiceItem> findByItemId(Long itemId);
    void deleteByInvoiceId(Long invoiceId);
}
