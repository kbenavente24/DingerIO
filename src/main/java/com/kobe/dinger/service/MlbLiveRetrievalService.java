package com.kobe.dinger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kobe.dinger.DTOs.livegamefeed.AllPlaysDTO;
import com.kobe.dinger.DTOs.livegamefeed.LinescoreDTO;
import com.kobe.dinger.DTOs.livegamefeed.LiveFeedResponseDTO;
import com.kobe.dinger.model.GameState;
import com.kobe.dinger.model.NotificationEvent;
import com.kobe.dinger.model.TeamSubscription;

@Service
public class MlbLiveRetrievalService {
    private RestTemplate restTemplate = new RestTemplate();
    private NotificationService notificationService;

    public MlbLiveRetrievalService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void processGame(Integer gamePk, List<TeamSubscription> awaySubscriptions, List<TeamSubscription> homeSubscriptions, Map<Integer, GameState> lastGameState) {
        String url = "https://statsapi.mlb.com/api/v1.1/game/" + gamePk + "/feed/live";
        LiveFeedResponseDTO feed = restTemplate.getForObject(url, LiveFeedResponseDTO.class);

        if (feed == null || feed.getLiveData() == null || feed.getLiveData().getLinescore() == null) {
            return;
        }

        String awayName = feed.getGameData().getTeams().getAway().getName();
        String homeName = feed.getGameData().getTeams().getHome().getName();

        LinescoreDTO linescore = feed.getLiveData().getLinescore();
        if (linescore.getCurrentInning() == null || linescore.getTeams() == null) {
            return;
        }

        

        int currentInning = linescore.getCurrentInning();
        String inningHalf = linescore.getInningHalf();

        List<Integer> scoringPlays = new ArrayList<>();
        if(feed.getScoringPlays() != null){
            scoringPlays.addAll(feed.getScoringPlays());
        }

        int awayScore = 0;
        if (linescore.getTeams().getAway().getRuns() != null) {
            awayScore = linescore.getTeams().getAway().getRuns();
        }

        int homeScore = 0;
        if (linescore.getTeams().getHome().getRuns() != null) {
            homeScore = linescore.getTeams().getHome().getRuns();
        }

        int awayHomeRuns = 0;
        if (linescore.getTeams().getAway().getHomeRuns() != null) {
            awayHomeRuns = linescore.getTeams().getAway().getHomeRuns();
        }

        int homeHomeRuns = 0;
        if (linescore.getTeams().getHome().getHomeRuns() != null) {
            homeHomeRuns = linescore.getTeams().getHome().getHomeRuns();
        }
        int homeRunCount = awayHomeRuns + homeHomeRuns;

        int awayHits = 0;
        if (linescore.getTeams().getAway().getHits() != null){
            awayHits = linescore.getTeams().getAway().getHits();
        }
        int homeHits = 0;
        if (linescore.getTeams().getHome().getHits() != null){
            homeHits = linescore.getTeams().getHome().getHits();
        }

        GameState previous = lastGameState.get(gamePk);

        // first time seeing this game — store state without notifying to avoid a flood on startup
        if (previous == null) {
            lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeRunCount, awayHits, homeHits, scoringPlays));
            return;
        }

        List<TeamSubscription> allSubscriptions = new ArrayList<>(awaySubscriptions);
        allSubscriptions.addAll(homeSubscriptions);

        boolean inningChanged = currentInning > previous.getCurrentInning();
        boolean halfChanged = inningChanged || !inningHalf.equals(previous.getInningHalf());
        boolean homeRunScored = homeRunCount > previous.getHomeRunCount();
        boolean awayHitOccured = awayHits > previous.getAwayHits();
        boolean homeHitOccured = homeHits > previous.getHomeHits();
        boolean scoreChanged = previous.getScoringPlays().size() < scoringPlays.size(); 

        for (TeamSubscription sub : allSubscriptions) {
            Set<NotificationEvent> events = sub.getNotificationEvents();

            if (inningChanged && events.contains(NotificationEvent.INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }
            if (halfChanged && !inningChanged && events.contains(NotificationEvent.HALF_INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }
            if (scoreChanged && events.contains(NotificationEvent.SCORE_CHANGE)) {
                List<AllPlaysDTO> allPlays = feed.getLiveData().getAllPlays();
                int lastScoringPlayID = scoringPlays.getLast();

                for(int i = allPlays.size() - 1; i >= 0; i--){
                    if(allPlays.get(i).getAbout().getAtBatIndex() == lastScoringPlayID){
                        notificationService.sendNotification(sub, allPlays.get(i).getResult().getDescription());
                        break;
                    }
                }
            }
            if (homeRunScored && events.contains(NotificationEvent.HOMERUN)) {
                notificationService.sendNotification(sub, "Home run! Away: " + awayScore + ", Home: " + homeScore);
            }
            if (awayHitOccured && events.contains(NotificationEvent.HIT)) {
                notificationService.sendNotification(sub, awayName + " made a hit!");
            }
            if (homeHitOccured && events.contains(NotificationEvent.HIT)) {
                notificationService.sendNotification(sub, homeName + " made a hit!");
            }
        }

        lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeRunCount, awayHits, homeHits, scoringPlays));
    }
    
    public void processGameEnd(Integer gamePk, List<TeamSubscription> awaySubscriptions, List<TeamSubscription> homeSubscriptions, Map<Integer, GameState> lastGameState){
        GameState previous = lastGameState.get(gamePk);

        // if previous == null then the game is not being tracked, meaning a final game notification was sent already and gamePk was removed from lastGameState hashmap in 
        // GamePollingService class
        if (previous == null) {
            return;
        }
        String url = "https://statsapi.mlb.com/api/v1.1/game/" + gamePk + "/feed/live";
        LiveFeedResponseDTO feed = restTemplate.getForObject(url, LiveFeedResponseDTO.class);

        if (feed == null || feed.getLiveData() == null || feed.getLiveData().getLinescore() == null) {
            return;
        }

        String awayName = feed.getGameData().getTeams().getAway().getName();
        String homeName = feed.getGameData().getTeams().getHome().getName();

        LinescoreDTO linescore = feed.getLiveData().getLinescore();
        if (linescore.getCurrentInning() == null || linescore.getTeams() == null) {
            return;
        }

        List<TeamSubscription> allSubscriptions = new ArrayList<>(awaySubscriptions);
        allSubscriptions.addAll(homeSubscriptions);

        for (TeamSubscription sub : allSubscriptions) {
            Set<NotificationEvent> events = sub.getNotificationEvents();
            if (events.contains(NotificationEvent.GAME_END)) {
                notificationService.sendNotification(sub, "Game has ended! Final Score: " + homeName + ": " + feed.getLiveData().getLinescore().getTeams().getHome().getRuns()
            + " - " + awayName + ": " + feed.getLiveData().getLinescore().getTeams().getAway().getRuns());
            }
        }
        lastGameState.remove(gamePk);
    }
}
