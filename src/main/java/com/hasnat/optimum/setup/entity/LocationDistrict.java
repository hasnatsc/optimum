package com.hasnat.optimum.setup.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "stp_location_districts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationDistrict {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)   private String code;
    @Column(nullable = false, length = 255)  private String name;
    @Column(length = 150)                    private String nameNative;
    @Column(nullable = false)                private boolean active;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private LocationState state;
}
