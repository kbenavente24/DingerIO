package com.kobe.dinger.DTOs.livegamefeed;

public class GameDataTeamsDTO {
    private GameDataTeamDTO away;
    private GameDataTeamDTO home;

    public GameDataTeamDTO getAway(){
        return this.away;
    }
    public void setAway(GameDataTeamDTO away){
        this.away = away;
    }
    public GameDataTeamDTO getHome(){
        return this.home;
    }
    public void setHome(GameDataTeamDTO home){
        this.home = home;
    }
    
}
