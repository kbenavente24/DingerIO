package com.kobe.dinger.DTOs.livegamefeed;

public class GameDataDTO {
    private GameDataTeamsDTO teams;
    private StatusDTO status;
    private ProbablePitchersDTO probablePitchers;

    public StatusDTO getStatus() {
        return status;
    }

    public void setStatus(StatusDTO status) {
        this.status = status;
    }

    public GameDataTeamsDTO getTeams(){
        return this.teams;
    }

    public void setTeams(GameDataTeamsDTO teams){
        this.teams = teams;
    }

    public ProbablePitchersDTO getProbablePitchers() {
        return probablePitchers;
    }

    public void setProbablePitchers(ProbablePitchersDTO probablePitchers) {
        this.probablePitchers = probablePitchers;
    }
}
