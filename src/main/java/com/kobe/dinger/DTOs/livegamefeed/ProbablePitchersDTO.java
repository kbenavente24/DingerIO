package com.kobe.dinger.DTOs.livegamefeed;

public class ProbablePitchersDTO {
    private ProbablePitchersTeamWrapperDTO home;
    private ProbablePitchersTeamWrapperDTO away;
    
    public ProbablePitchersTeamWrapperDTO getHome() {
        return home;
    }
    public void setHome(ProbablePitchersTeamWrapperDTO home) {
        this.home = home;
    }
    public ProbablePitchersTeamWrapperDTO getAway() {
        return away;
    }
    public void setAway(ProbablePitchersTeamWrapperDTO away) {
        this.away = away;
    }


}
