package com.hasnat.optimum.setup.entity;

import com.hasnat.optimum.common.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stp_terms_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TermsMaster {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentType documentType;

    private Boolean isActive;
    private Boolean isDefault;
    private Integer sortOrder;
}
