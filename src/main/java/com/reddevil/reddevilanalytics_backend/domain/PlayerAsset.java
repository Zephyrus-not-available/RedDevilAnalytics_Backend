package com.reddevil.reddevilanalytics_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "player_assets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_player_asset", columnNames = {"player_id", "provider"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "cutout_url", length = 500)
    private String cutoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Provider provider;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
