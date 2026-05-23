package com.hasnat.optimum.setup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stp_currencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Currency {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 10)
    private String symbol;

    @Column(nullable = false)
    private int decimalPlaces;

    @Column(nullable = false)
    private boolean active;
}
