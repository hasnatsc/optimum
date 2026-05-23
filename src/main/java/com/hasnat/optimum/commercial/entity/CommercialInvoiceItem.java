package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.inventory.entity.Item;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "com_commercial_invoice_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommercialInvoiceItem {

    @Id private Long id;

    @Column(nullable = false, precision = 18, scale = 3) private BigDecimal quantity;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal unitPrice;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal totalAmount;
    @Column(length = 500) private String description;
    @Column(length = 20)  private String unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private CommercialInvoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Stub FK — Sales module
    @Column(name = "delivery_detail_id") private Long deliveryDetailId;
}
