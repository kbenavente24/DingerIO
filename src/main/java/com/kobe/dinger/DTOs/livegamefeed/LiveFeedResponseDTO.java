package com.kobe.dinger.DTOs.livegamefeed;

import java.util.List;

public class LiveFeedResponseDTO {
    private LiveDataDTO liveData;
    private GameDataDTO gameData;
    private List<Integer> scoringPlays;

    public LiveDataDTO getLiveData() { return liveData; }
    public void setLiveData(LiveDataDTO liveData) { this.liveData = liveData; }

    public GameDataDTO getGameData() {return gameData; }
    public void setGameData(GameDataDTO gameData){ this.gameData = gameData;}

    public List<Integer> getScoringPlays(){ return scoringPlays; }
    public void setScoringPlays(List<Integer> scoringPlays){ this.scoringPlays = scoringPlays;}

}
