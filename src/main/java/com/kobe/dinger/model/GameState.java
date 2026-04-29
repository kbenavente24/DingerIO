package com.kobe.dinger.model;

import java.util.List;

public class GameState {
    private int currentInning;
    private String inningHalf;
    private int awayScore;
    private int homeScore;
    private int homeHomeRunCount;
    private int awayHomeRunCount;
    private int homeRunCount;
    private List<Integer> scoringPlays;

    public GameState(int currentInning, String inningHalf, int awayScore, int homeScore, int homeHomeRunCount, int awayHomeRunCount, int homeRunCount, List<Integer> scoringPlays) {
        this.currentInning = currentInning;
        this.inningHalf = inningHalf;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
        this.homeRunCount = homeRunCount;
        this.scoringPlays = scoringPlays;
    }

    public int getCurrentInning() { return currentInning; }
    public void setCurrentInning(int currentInning) { this.currentInning = currentInning; }

    public String getInningHalf() { return inningHalf; }
    public void setInningHalf(String inningHalf) { this.inningHalf = inningHalf; }

    public int getAwayScore() { return awayScore; }
    public void setAwayScore(int awayScore) { this.awayScore = awayScore; }

    public int getHomeScore() { return homeScore; }
    public void setHomeScore(int homeScore) { this.homeScore = homeScore; }

    public int getHomeRunCount() { return homeRunCount; }
    public void setHomeRunCount(int homeRunCount) { this.homeRunCount = homeRunCount; }

    public List<Integer> getScoringPlays(){return scoringPlays;}
    public void setScoringPlays(List<Integer> scoringPlays){this.scoringPlays = scoringPlays;}

    public int getHomeHomeRunCount() { return homeHomeRunCount; }
    public void setHomeHomeRunCount(int homeHomeRunCount) { this.homeHomeRunCount = homeHomeRunCount; }

    public int getAwayHomeRunCount() { return awayHomeRunCount; }
    public void setAwayHomeRunCount(int awayHomeRunCount) { this.awayHomeRunCount = awayHomeRunCount; }

    

}
