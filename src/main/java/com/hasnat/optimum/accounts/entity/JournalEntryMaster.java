package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.VoucherType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Stub entity — full General Ledger module schema not yet provided.
 * Defined here because apr_voucher.journal_entry_master_id references this table.
 */
@Entity @Table(name = "acc_journal_entry_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JournalEntryMaster extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100) private String voucherNo;
    private LocalDate voucherDate;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private VoucherType voucherType;

    @Column(precision = 18, scale = 2) private BigDecimal totalDebit;
    @Column(precision = 18, scale = 2) private BigDecimal totalCredit;

    private Boolean isPosted;
    @Column(length = 100) private String postedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
