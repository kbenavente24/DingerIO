package com.kobe.dinger.DTOs.livegamefeed;

import java.util.List;

public class PlaysDTO {
    List<AllPlaysDTO> allPlays;
    List<Integer> scoringPlays;
    CurrentPlayDTO currentPlay;

    public CurrentPlayDTO getCurrentPlay() {
        return currentPlay;
    }
    public void setCurrentPlay(CurrentPlayDTO currentPlay) {
        this.currentPlay = currentPlay;
    }
    public List<AllPlaysDTO> getAllPlays(){
        return allPlays;
    }
    public void setAllPlays(List<AllPlaysDTO> allPlays){
        this.allPlays = allPlays;
    }

    public List<Integer> getScoringPlays(){
        return scoringPlays;
    }
    public void setScoringPlays(List<Integer> scoringPlays){
        this.scoringPlays = scoringPlays;
    }
}
