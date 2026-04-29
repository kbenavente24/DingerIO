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
import com.kobe.dinger.model.Team;
import com.kobe.dinger.model.TeamSubscription;

@Service
public class MlbLiveRetrievalService {
    private RestTemplate restTemplate = new RestTemplate();
    private NotificationService notificationService;

    public MlbLiveRetrievalService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void processGame(Integer gamePk, List<TeamSubscription> subscriptions,  Map<Integer, GameState> lastGameState, Team homeTeam, Team awayTeam) {
        String url = "https://statsapi.mlb.com/api/v1.1/game/" + gamePk + "/feed/live";
        LiveFeedResponseDTO feed = restTemplate.getForObject(url, LiveFeedResponseDTO.class);

        if (feed == null || feed.getLiveData() == null || feed.getLiveData().getLinescore() == null) {
            return;
        }

        LinescoreDTO linescore = feed.getLiveData().getLinescore();
        if (linescore.getCurrentInning() == null || linescore.getTeams() == null) {
            return;
        }

    
        int currentInning = linescore.getCurrentInning();
        String inningHalf = linescore.getInningHalf();

        List<Integer> scoringPlays = new ArrayList<>();
        if(feed.getLiveData().getPlays().getScoringPlays() != null){
            scoringPlays.addAll(feed.getLiveData().getPlays().getScoringPlays());
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

        GameState previous = lastGameState.get(gamePk);

        // first time seeing this game — store state without notifying to avoid a flood on startup
        if (previous == null) {
            lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeHomeRuns, awayHomeRuns, homeRunCount, scoringPlays));
            return;
        }

        //Top inning = true means away team is batting. 

        boolean inningChanged = currentInning > previous.getCurrentInning();
        boolean halfChanged = inningChanged || !inningHalf.equals(previous.getInningHalf());
        boolean homeHomeRunScored = homeHomeRuns > previous.getHomeHomeRunCount();
        boolean awayHomeRunScored = awayHomeRuns > previous.getAwayHomeRunCount();
        boolean homeRunScored = homeRunCount > previous.getHomeRunCount();
        boolean scoreChanged = previous.getScoringPlays().size() < scoringPlays.size(); 

        for (TeamSubscription sub : subscriptions) {
        
            Set<NotificationEvent> events = sub.getNotificationEvents();


            //notify on every inning change
            if (inningChanged && events.contains(NotificationEvent.INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }

            //notify on every inning + top and bottom
            if (halfChanged && !inningChanged && events.contains(NotificationEvent.HALF_INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }

            //notify on every score change
            if (scoreChanged && events.contains(NotificationEvent.SCORE_CHANGE)) {
                System.out.println("HOME RUN SCORED BOOLEAN: " + homeHomeRunScored);
                System.out.println("AWAY HOME RUN SCORED BOOLEAN: " + awayHomeRunScored);
                List<AllPlaysDTO> allPlays = feed.getLiveData().getPlays().getAllPlays();
                int lastScoringPlayID = scoringPlays.getLast();

                for(int i = allPlays.size() - 1; i >= 0; i--){
                    if(allPlays.get(i).getAbout().getAtBatIndex() == lastScoringPlayID){
                        if("home_run".equals(allPlays.get(i).getResult().getEventType())){
                            if(sub.getTeam().equals(homeTeam) && "bottom".equals(allPlays.get(i).getAbout().getHalfInning())){
                                notificationService.sendNotification(sub, "YOUR TEAM HIT A HOME RUN!\n" + allPlays.get(i).getResult().getDescription());
                                break;
                            } else if(sub.getTeam().equals(awayTeam) && "top".equals(allPlays.get(i).getAbout().getHalfInning())){
                                notificationService.sendNotification(sub, "YOUR TEAM HIT A HOME RUN!\n" + allPlays.get(i).getResult().getDescription());
                                break;
                            } else {
                                notificationService.sendNotification(sub, allPlays.get(i).getResult().getDescription());
                                break;                                 
                            }
                        } else {
                            notificationService.sendNotification(sub, allPlays.get(i).getResult().getDescription());
                            break;                            
                        }
                    }
                }
            }
        }

        lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeHomeRuns, awayHomeRuns, homeRunCount, scoringPlays));
    }
    
    public void processGameEnd(Integer gamePk, List<TeamSubscription> subscriptions,  Map<Integer, GameState> lastGameState){
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

        for (TeamSubscription sub : subscriptions) {
            Set<NotificationEvent> events = sub.getNotificationEvents();
            if (events.contains(NotificationEvent.GAME_END)) {
                notificationService.sendNotification(sub, "Game has ended! Final Score: " + homeName + ": " + feed.getLiveData().getLinescore().getTeams().getHome().getRuns()
            + " - " + awayName + ": " + feed.getLiveData().getLinescore().getTeams().getAway().getRuns());
            }
        }
        lastGameState.remove(gamePk);
    }
}
