package com.kobe.dinger.model;

import java.util.List;

public class GameState {
    private int currentInning;
    private String inningHalf;
    private List<Integer> scoringPlays;

    public GameState(int currentInning, String inningHalf, List<Integer> scoringPlays) {
        this.currentInning = currentInning;
        this.inningHalf = inningHalf;
        this.scoringPlays = scoringPlays;
    }

    public int getCurrentInning() { return currentInning; }
    public void setCurrentInning(int currentInning) { this.currentInning = currentInning; }

    public String getInningHalf() { return inningHalf; }
    public void setInningHalf(String inningHalf) { this.inningHalf = inningHalf; }

    public List<Integer> getScoringPlays(){return scoringPlays;}
    public void setScoringPlays(List<Integer> scoringPlays){this.scoringPlays = scoringPlays;}

}
