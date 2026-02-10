package com.reddevil.reddevilanalytics_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match_predictions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_prediction", columnNames = {"match_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "home_win_probability", precision = 5, scale = 2)
    private BigDecimal homeWinProbability;

    @Column(name = "draw_probability", precision = 5, scale = 2)
    private BigDecimal drawProbability;

    @Column(name = "away_win_probability", precision = 5, scale = 2)
    private BigDecimal awayWinProbability;

    @Column(name = "predicted_home_score", precision = 4, scale = 2)
    private BigDecimal predictedHomeScore;

    @Column(name = "predicted_away_score", precision = 4, scale = 2)
    private BigDecimal predictedAwayScore;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
