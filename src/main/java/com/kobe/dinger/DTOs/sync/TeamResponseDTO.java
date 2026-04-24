package com.kobe.dinger.DTOs.sync;

import java.util.List;

public class TeamResponseDTO {
    private List<TeamDTO> teams;

    public List<TeamDTO> getTeams(){
        return this.teams;
    }

    public void setTeams(List<TeamDTO> teams){
        this.teams = teams;
    }
}
