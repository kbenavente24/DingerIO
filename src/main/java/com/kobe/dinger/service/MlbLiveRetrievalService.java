package com.kobe.dinger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kobe.dinger.DTOs.LiveFeedResponseDTO;
import com.kobe.dinger.DTOs.LinescoreDTO;
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

        LinescoreDTO linescore = feed.getLiveData().getLinescore();
        if (linescore.getCurrentInning() == null || linescore.getTeams() == null) {
            return;
        }

        int currentInning = linescore.getCurrentInning();
        String inningHalf = linescore.getInningHalf();
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
            lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeRunCount));
            return;
        }

        List<TeamSubscription> allSubscriptions = new ArrayList<>(awaySubscriptions);
        allSubscriptions.addAll(homeSubscriptions);

        boolean inningChanged = currentInning > previous.getCurrentInning();
        boolean halfChanged = inningChanged || !inningHalf.equals(previous.getInningHalf());
        boolean scoreChanged = awayScore != previous.getAwayScore() || homeScore != previous.getHomeScore();
        boolean homeRunScored = homeRunCount > previous.getHomeRunCount();

        for (TeamSubscription sub : allSubscriptions) {
            Set<NotificationEvent> events = sub.getNotificationEvents();

            if (inningChanged && events.contains(NotificationEvent.INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }
            if (halfChanged && !inningChanged && events.contains(NotificationEvent.HALF_INNING_CHANGE)) {
                notificationService.sendNotification(sub, inningHalf + " of inning " + currentInning + " has started!");
            }
            if (scoreChanged && events.contains(NotificationEvent.SCORE_CHANGE)) {
                notificationService.sendNotification(sub, "Score update — Away: " + awayScore + ", Home: " + homeScore);
            }
            if (homeRunScored && events.contains(NotificationEvent.HOMERUN)) {
                notificationService.sendNotification(sub, "Home run! Away: " + awayScore + ", Home: " + homeScore);
            }
        }

        lastGameState.put(gamePk, new GameState(currentInning, inningHalf, awayScore, homeScore, homeRunCount));
    }
}
