package com.kobe.dinger.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.kobe.dinger.DTOs.schedule.DateDTO;
import com.kobe.dinger.DTOs.schedule.GameDTO;
import com.kobe.dinger.DTOs.schedule.ScheduleResponseDTO;
import com.kobe.dinger.model.GameState;
import com.kobe.dinger.model.Team;
import com.kobe.dinger.model.TeamSubscription;
import com.kobe.dinger.repository.TeamRepository;
import com.kobe.dinger.repository.TeamSubscriptionRepository;

@Service
public class GamePollingService {
    private static final Logger log = LoggerFactory.getLogger(GamePollingService.class);
    private TeamSubscriptionRepository teamSubscriptionRepository;
    private TeamRepository teamRepository;
    private MlbLiveRetrievalService mlbLiveRetrievalService;
    private NotificationService notificationService;
    private RestTemplate restTemplate = new RestTemplate();
    private Map<Integer, GameState> lastGameState = new HashMap<>();

    public GamePollingService(TeamSubscriptionRepository teamSubscriptionRepository, TeamRepository teamRepository, MlbLiveRetrievalService mlbLiveRetrievalService,
        NotificationService notificationService
    ){
        this.teamSubscriptionRepository = teamSubscriptionRepository;
        this.teamRepository = teamRepository;
        this.mlbLiveRetrievalService = mlbLiveRetrievalService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 30000)
    public void pollGames(){
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=" + today;
        log.info("Polling games for {}", today);

        //ScheduleResponseDTO has list of Date DTOs -> DateDTO has list of GameDTOs -> GameDTO has GamePK and DTOs for status (live, ended, etc), and teams (away and home)
        //Summary: This DTO is used for getting the games for the current day in order to process their live data
        ScheduleResponseDTO schedule = restTemplate.getForObject(url, ScheduleResponseDTO.class);

        if (schedule == null || schedule.getDates() == null || schedule.getDates().isEmpty()) {
            log.info("No games scheduled today");
            return;
        }

        List<GameDTO> games = schedule.getDates().get(0).getGames();
        log.info("Found {} games today", games.size());

        for (GameDTO game : games) {
            if (!"Live".equals(game.getStatus().getAbstractGameState()) && !"Final".equals(game.getStatus().getAbstractGameState())) {
                continue;
            }
            Integer awayTeamMlbId = game.getTeams().getAway().getTeam().getId();
            Integer homeTeamMlbId = game.getTeams().getHome().getTeam().getId();

            Team awayTeam = teamRepository.findByMlbTeamId(awayTeamMlbId).orElse(null);
            Team homeTeam = teamRepository.findByMlbTeamId(homeTeamMlbId).orElse(null);

            List<TeamSubscription> awaySubscriptions;
            if (awayTeam != null) {
                awaySubscriptions = teamSubscriptionRepository.findByTeam(awayTeam);
            } else {
                awaySubscriptions = List.of();
            }

            List<TeamSubscription> homeSubscriptions;
            if (homeTeam != null) {
                homeSubscriptions = teamSubscriptionRepository.findByTeam(homeTeam);
            } else {
                homeSubscriptions = List.of();
            }

            if (awaySubscriptions.isEmpty() && homeSubscriptions.isEmpty()) {
                log.info("No subscriptions for gamePk={}, skipping", game.getGamePk());
                continue;
            }

            log.info("Processing gamePk={} — {} away subs, {} home subs", game.getGamePk(), awaySubscriptions.size(), homeSubscriptions.size());
            if ("Final".equals(game.getStatus().getAbstractGameState())) {
                mlbLiveRetrievalService.processGameEnd(game.getGamePk(),awaySubscriptions, homeSubscriptions, lastGameState);
            } else {
                mlbLiveRetrievalService.processGame(game.getGamePk(), awaySubscriptions, homeSubscriptions, lastGameState);
            }
        }
    }
}
