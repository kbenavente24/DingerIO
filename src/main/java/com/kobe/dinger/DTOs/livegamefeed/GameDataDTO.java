package com.kobe.dinger.DTOs;

public class GameDataDTO {
    private GameDataTeamsDTO teams;

    public GameDataTeamsDTO getTeams(){
        return this.teams;
    }

    public void setTeams(GameDataTeamsDTO teams){
        this.teams = teams;
    }
}
