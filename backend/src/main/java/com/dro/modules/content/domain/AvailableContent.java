package com.dro.modules.content.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "available_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "release_order", nullable = false)
    private int releaseOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}