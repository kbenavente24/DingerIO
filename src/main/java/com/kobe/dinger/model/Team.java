package com.kobe.dinger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "team")
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;

    private Integer mlbTeamId;

    private String teamName;

    private String logoImageUrl;

    public Team() {}

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public Integer getMlbTeamId() { return mlbTeamId; }
    public void setMlbTeamId(Integer mlbTeamId) { this.mlbTeamId = mlbTeamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getLogoImageUrl() { return logoImageUrl; }
    public void setLogoImageUrl(String logoImageUrl) { this.logoImageUrl = logoImageUrl; }
}
