package com.hasnat.optimum.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name = "sec_permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 255) private String urlPattern;
    @Column(nullable = false, length = 10)  private String httpMethod;
    @Column(length = 255)                   private String description;
    @Column(length = 50)                    private String category;

    @Enumerated(EnumType.STRING) @Column(length = 50)
    private Module module;

    @Column(nullable = false) private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
