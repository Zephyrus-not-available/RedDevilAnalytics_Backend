package com.reddevil.reddevilanalytics_backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String competition;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    protected Match() {
    }

    public Match(Team homeTeam, Team awayTeam, LocalDate date, String competition, Integer homeScore, Integer awayScore) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.date = date;
        this.competition = competition;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public Long getId() {
        return id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCompetition() {
        return competition;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }
}

