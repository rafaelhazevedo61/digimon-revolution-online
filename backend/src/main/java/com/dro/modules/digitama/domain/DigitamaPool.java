package com.dro.modules.digitama.domain;

import com.dro.modules.content.domain.AvailableContent;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "digitama_pools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitamaPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private AvailableContent content;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "digitamaPool", fetch = FetchType.LAZY)
    private List<DigitamaPoolEntry> entries = new ArrayList<>();
}