package com.hasnat.optimum.organization.entity;

import com.hasnat.optimum.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity @Table(name = "org_organizations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Organization extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "text")
    private String about;

    @Column(columnDefinition = "text")
    private String address;

    @Column(length = 100) private String city;
    @Column(length = 100) private String state;
    @Column(length = 100) private String country;
    @Column(length = 20)  private String postalCode;
    @Column(length = 100) private String email;
    @Column(length = 20)  private String phone;
    @Column(length = 255) private String website;
    @Column(length = 500) private String logoUrl;

    private LocalDate establishedDate;

    @Column(nullable = false)
    private boolean isActive;
}
