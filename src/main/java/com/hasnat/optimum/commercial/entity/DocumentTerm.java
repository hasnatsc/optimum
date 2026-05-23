package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.global.entity.BusinessDocument;
import jakarta.persistence.*;
import lombok.*;

/**
 * FIXED: document_id FK now references BusinessDocument (was gbl_order_management — undefined table).
 */
@Entity @Table(name = "com_document_terms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentTerm {

    @Id private Long id;

    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "text") private String description;
    private Long globalTermsId; // soft ref to stp_terms_master
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private BusinessDocument document; // FIXED: was gbl_order_management

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private CommercialInvoice invoice;
}
