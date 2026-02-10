package com.reddevil.reddevilanalytics_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "standings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_standing", columnNames = {"competition_id", "season_id", "team_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "played_games")
    @Builder.Default
    private Integer playedGames = 0;

    @Builder.Default
    private Integer won = 0;

    @Builder.Default
    private Integer draw = 0;

    @Builder.Default
    private Integer lost = 0;

    @Builder.Default
    private Integer points = 0;

    @Column(name = "goals_for")
    @Builder.Default
    private Integer goalsFor = 0;

    @Column(name = "goals_against")
    @Builder.Default
    private Integer goalsAgainst = 0;

    @Column(name = "goal_difference")
    @Builder.Default
    private Integer goalDifference = 0;

    @Column(length = 50)
    private String form;

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
