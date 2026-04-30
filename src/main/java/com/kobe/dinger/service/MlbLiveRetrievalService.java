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

        int currentHomeScore = linescore.getTeams().getHome().getRuns();
        int currentAwayScore = linescore.getTeams().getAway().getRuns();
        int currentInning = linescore.getCurrentInning();
        String inningHalf = linescore.getInningHalf();

        List<Integer> scoringPlays = new ArrayList<>();
        if(feed.getLiveData().getPlays().getScoringPlays() != null){
            scoringPlays.addAll(feed.getLiveData().getPlays().getScoringPlays());
        }

        GameState previous = lastGameState.get(gamePk);

        // first time seeing this game — store state without notifying to avoid a flood on startup
        if (previous == null) {
            lastGameState.put(gamePk, new GameState(currentInning, inningHalf, scoringPlays));
            return;
        }

        boolean inningChanged = currentInning > previous.getCurrentInning();
        boolean halfChanged = inningChanged || !inningHalf.equals(previous.getInningHalf());

        
        boolean scoreChanged = previous.getScoringPlays().size() < scoringPlays.size(); 
        //Fields that change based on if the score was changed
        List<AllPlaysDTO> allPlays;
        int lastScoringPlayId;
        int latestHomeScore = 0;
        int latestAwayScore = 0;
        boolean homeRunScored = false;
        boolean homeTeamScored = false;
        boolean awayTeamScored = false;
        String scoringPlayDescription = "";

        if(scoreChanged){
            allPlays = feed.getLiveData().getPlays().getAllPlays();
            lastScoringPlayId = scoringPlays.getLast();        
            for(int i = allPlays.size() - 1; i >= 0; i--){
                if(allPlays.get(i).getAbout().getAtBatIndex() == lastScoringPlayId){
                    latestHomeScore = allPlays.get(i).getResult().getHomeScore();
                    latestAwayScore = allPlays.get(i).getResult().getAwayScore();

                    if("home_run".equals(allPlays.get(i).getResult().getEventType())){
                        homeRunScored = true;
                    }

                    if("bottom".equals(allPlays.get(i).getAbout().getHalfInning())){
                        homeTeamScored = true;
                    }

                    if("top".equals(allPlays.get(i).getAbout().getHalfInning())){
                        awayTeamScored = true;
                    }
                    scoringPlayDescription = allPlays.get(i).getResult().getDescription();

                    break;
                }
            }
        }

        for (TeamSubscription sub : subscriptions) {
            Set<NotificationEvent> events = sub.getNotificationEvents();
            boolean subbedTeamIsHomeTeam = sub.getTeam().equals(homeTeam);

            //notify on every inning change
            if (inningChanged && events.contains(NotificationEvent.INNING_CHANGE)) {
                if(currentInning > 1){
                    notificationService.sendNotification(sub, "Inning " + Integer.toString(currentInning - 1) + " has ended \n" +
                    "Score heading into the " + inningHalf + " of inning " + currentInning + ": " + 
                    generateLineScores(subbedTeamIsHomeTeam, currentHomeScore, currentAwayScore, homeTeam, awayTeam));
                }
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }

            //notify on every inning + top and bottom
            if (halfChanged && !inningChanged && events.contains(NotificationEvent.HALF_INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }
            
            //notify on every score change
            if (scoreChanged && events.contains(NotificationEvent.SCORE_CHANGE)) {
                String message = generateScoringMessage(scoringPlayDescription, latestHomeScore, latestAwayScore, homeRunScored, homeTeamScored, awayTeamScored,homeTeam, awayTeam,
                    subbedTeamIsHomeTeam);
                notificationService.sendNotification(sub, message);
            }
        }
        lastGameState.put(gamePk, new GameState(currentInning, inningHalf, scoringPlays));
    }

    private String generateScoringMessage(String scoringPlayDescription, int latestHomeScore, int latestAwayScore, boolean homeRunScored, boolean homeTeamScored, boolean awayTeamScored, Team homeTeam,Team awayTeam, boolean subbedTeamIsHomeTeam){

        String scoringMessage = "";

        if(homeRunScored){
            if(subbedTeamIsHomeTeam && homeTeamScored){
                scoringMessage = homeTeam.getTeamEmoji() + "  HOME RUN!  "+ homeTeam.getTeamEmoji() + "\n" 
                + scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);        
            } else if(!subbedTeamIsHomeTeam && awayTeamScored){
                scoringMessage = awayTeam.getTeamEmoji() +  "  HOME RUN!  " + awayTeam.getTeamEmoji() + "\n" 
                + scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);          
            } else {
                scoringMessage = scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);
            } 
        } else {
            if(subbedTeamIsHomeTeam && homeTeamScored){
                scoringMessage = homeTeam.getTeamEmoji() + homeTeam.getTeamName() + "  score!  " + homeTeam.getTeamEmoji() + "\n"
                + scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);
            } else if (!subbedTeamIsHomeTeam && awayTeamScored){
                scoringMessage = awayTeam.getTeamEmoji() + awayTeam.getTeamName() + "  score!  " + awayTeam.getTeamEmoji() + "\n"
                + scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);
            } else {
                scoringMessage = scoringPlayDescription + "\n" + generateLineScores(subbedTeamIsHomeTeam, latestHomeScore, latestAwayScore, homeTeam, awayTeam);
            }
        }
        return scoringMessage;
    }

    private String generateLineScores(boolean subbedTeamIsHomeTeam, int latestHomeScore, int latestAwayScore, Team homeTeam, Team awayTeam){
        String lineScoreMessage = "";
            if(subbedTeamIsHomeTeam){
                lineScoreMessage = homeTeam.getTeamName() + ": " + latestHomeScore + " | " + awayTeam.getTeamName() + ": " + latestAwayScore;     
            } else {
                lineScoreMessage = awayTeam.getTeamName() + ": " + latestAwayScore + " | " + homeTeam.getTeamName()
                + ": " + latestHomeScore;        
            }
        return lineScoreMessage;
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
