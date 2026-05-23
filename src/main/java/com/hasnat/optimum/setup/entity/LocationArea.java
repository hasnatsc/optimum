package com.hasnat.optimum.setup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stp_location_areas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationArea {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255) private String name;
    @Column(length = 150)                   private String nameNative;
    @Column(nullable = false)               private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private LocationCity city;
}
