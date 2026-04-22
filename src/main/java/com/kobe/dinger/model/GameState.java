package com.kobe.dinger.model;

public class GameState {
    private int currentInning;
    private String inningHalf;
    private int awayScore;
    private int homeScore;
    private int homeRunCount;

    public GameState(int currentInning, String inningHalf, int awayScore, int homeScore, int homeRunCount) {
        this.currentInning = currentInning;
        this.inningHalf = inningHalf;
        this.awayScore = awayScore;
        this.homeScore = homeScore;
        this.homeRunCount = homeRunCount;
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
}
