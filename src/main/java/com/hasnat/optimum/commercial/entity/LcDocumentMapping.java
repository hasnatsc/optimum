package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.global.entity.BusinessDocument;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * FIXED: document_id FK now references BusinessDocument (was gbl_order_management).
 */
@Entity @Table(name = "com_lc_document_mapping")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LcDocumentMapping {

    @Id private Long id;

    @Column(precision = 18, scale = 2) private BigDecimal allocatedAmount;
    @Column(precision = 18, scale = 2) private BigDecimal utilizedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private BusinessDocument document; // FIXED: was gbl_order_management

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lc_id")
    private LetterOfCredit lc;
}
