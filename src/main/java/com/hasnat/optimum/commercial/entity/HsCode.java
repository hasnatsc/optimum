package com.hasnat.optimum.commercial.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import com.hasnat.optimum.organization.entity.Organization;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name = "com_hs_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HsCode extends BaseEntity {

    @Id private Long id;

    @Column(nullable = false, length = 20)   private String hsCode;
    @Column(nullable = false, length = 500)  private String description;
    @Column(length = 200)                    private String shortDescription;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private HsType hsType;

    @Column(precision = 6, scale = 2) private BigDecimal vatPercent;
    @Column(precision = 6, scale = 2) private BigDecimal customsDutyPercent;
    @Column(precision = 6, scale = 2) private BigDecimal supplementaryDutyPercent;
    @Column(precision = 6, scale = 2) private BigDecimal aitPercent;

    @Column(nullable = false) private boolean isActive;
    @Column(nullable = false) private boolean isBondedAllowed;
    @Column(nullable = false) private boolean requiresImportPermit;
    @Column(nullable = false) private boolean requiresExportPermit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public enum HsType { EXPORT, IMPORT, BOTH }
}
