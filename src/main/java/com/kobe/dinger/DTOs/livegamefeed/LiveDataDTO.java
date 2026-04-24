package com.kobe.dinger.DTOs.livegamefeed;

import java.util.List;

public class LiveDataDTO {
    private LinescoreDTO linescore;
    private List<AllPlaysDTO> allPlays;

    public LinescoreDTO getLinescore() { return linescore; }
    public void setLinescore(LinescoreDTO linescore) { this.linescore = linescore; }

    public List<AllPlaysDTO> getAllPlays(){return allPlays;}
    public void setAllPlays(List<AllPlaysDTO> allPlays){this.allPlays = allPlays;}
}
