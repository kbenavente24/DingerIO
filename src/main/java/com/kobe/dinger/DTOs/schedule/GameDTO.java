package com.kobe.dinger.DTOs.schedule;

public class GameDTO {
    private Integer gamePk;
    private GameStatusDTO status;
    private GameTeamsDTO teams;

    public Integer getGamePk() { return gamePk; }
    public void setGamePk(Integer gamePk) { this.gamePk = gamePk; }

    public GameStatusDTO getStatus() { return status; }
    public void setStatus(GameStatusDTO status) { this.status = status; }

    public GameTeamsDTO getTeams() { return teams; }
    public void setTeams(GameTeamsDTO teams) { this.teams = teams; }
}
