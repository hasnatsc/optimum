package com.hasnat.optimum.approval.entity;

import com.hasnat.optimum.accounts.entity.JournalEntryMaster;
import com.hasnat.optimum.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "apr_voucher")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalVoucher extends BaseEntity {

    @Id private Long id;

    @Column(nullable = false) private int approvalLevel;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ApprovalStatus approvalStatus;

    @Column(length = 100)  private String approverName;
    @Column(length = 100)  private String approverRole;
    @Column(length = 1000) private String approvalRemarks;
    private LocalDate approvalDate;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_master_id", nullable = false)
    private JournalEntryMaster journalEntryMaster;

    public enum ApprovalStatus { PENDING, APPROVED, REJECTED, CANCELLED }
}
