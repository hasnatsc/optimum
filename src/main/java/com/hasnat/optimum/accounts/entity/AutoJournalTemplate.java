package com.hasnat.optimum.accounts.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.common.enums.ModuleType;
import com.hasnat.optimum.common.enums.TransactionType;
import com.hasnat.optimum.common.enums.VoucherType;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "acc_auto_journal_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AutoJournalTemplate extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)  private String templateCode;
    @Column(nullable = false, length = 200) private String templateName;
    @Column(length = 500)                   private String description;
    @Column(length = 500)                   private String narrationTemplate;
    @Column(length = 500)                   private String narrationVariables;
    @Column(length = 50)                    private String cronExpression;
    @Column(length = 50)                    private String triggerEvent;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ModuleType moduleType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING) @Column(length = 30)
    private VoucherType voucherType;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TriggerMode triggerMode;

    @Enumerated(EnumType.STRING) @Column(length = 20)
    private RecurrenceType recurrenceType;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isSystem;
    @Column(nullable = false) private boolean autoPost;
    @Column(nullable = false) private boolean allowZeroLines;
    @Column(nullable = false) private boolean skipIfZeroTotal;
    @Column(nullable = false) private boolean requireConfirmation;
    @Column(nullable = false) private boolean validateBalance;

    private Integer recurrenceDay;
    private Integer recurrenceMonth;
    private Integer usageCount;
    private LocalDateTime nextRunDate;
    private LocalDateTime lastUsedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounts_policy_id")
    private AccountPolicy accountsPolicy;

    public enum TriggerMode { MANUAL, ON_APPROVAL, ON_POSTING, ON_COMPLETION, SCHEDULED, API_CALL }
    public enum RecurrenceType { DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY }
}
