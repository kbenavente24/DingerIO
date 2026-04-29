package com.kobe.dinger.DTOs.sync;

public class TeamDTO {
    private Integer id;
    private String teamName;

    public Integer getId(){
        return this.id;
    }

    public void setId(Integer id){
        this.id = id;
    }

    public String getTeamName(){
        return this.teamName;
    }

    public void setTeamName(String teamName){
        this.teamName = teamName;
    }
}
