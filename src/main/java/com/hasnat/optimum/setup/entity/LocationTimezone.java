package com.hasnat.optimum.setup.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "stp_location_timezones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationTimezone {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String zoneId;

    @Column(length = 255) private String description;
    @Column(nullable = false) private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
