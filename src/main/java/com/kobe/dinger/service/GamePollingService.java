package com.kobe.dinger.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import java.time.ZoneOffset;

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
    
    @Scheduled(cron = "0 0 9 * * MON", zone = "UTC")
    public void weeklyMondayPoll() {

        /*
        
        - get schedule for the week and pass it on:
        - get every team sub
        - pass on team subs
        

        -list of team subs
        -for(size of teams):
            -hit repo to check if sub exists for team, if it does, add to list of team subs
            -
        */

        LocalDate todayUTC = LocalDate.now(ZoneOffset.UTC);
        String todayToString = todayUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String scheduleForWeekUrl = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&startDate=" + todayToString + "&endDate=" + todayUTC.plusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        ScheduleResponseDTO schedule = restTemplate.getForObject(scheduleForWeekUrl, ScheduleResponseDTO.class);

        if (schedule == null || schedule.getDates() == null || schedule.getDates().isEmpty()) {
            log.info("No games scheduled today");
            return;
        }

        List<Team> teams = teamRepository.findAll();
        List<Integer> teamIds = teams.stream()
            .map(Team::getId)
            .collect(Collectors.toList());


        List<TeamSubscription> teamSubs = teamSubscriptionRepository.findByTeamIdIn(teamIds);
        List<DateDTO> dates = schedule.getDates();

        HashMap<Integer, String> teamScheduleStrings = new HashMap<>();

        for(DateDTO date : dates){
            for(GameDTO game : date.getGames()){
                teamScheduleStrings.put(game.getTeams().getHome().getTeam().getId(), teamScheduleStrings.getOrDefault(game.getTeams().getHome().getTeam().getId(), "") + date.getDate() + ": vs " + game.getTeams().getAway().getTeam().getName() + "\n");

                teamScheduleStrings.put(game.getTeams().getAway().getTeam().getId(), teamScheduleStrings.getOrDefault(game.getTeams().getAway().getTeam().getId(), "") + date.getDate() + ": vs" + game.getTeams().getHome().getTeam().getName() + "\n");
            }
        }

        for(TeamSubscription subscription : teamSubs){
            if(teamScheduleStrings.containsKey(subscription.getTeam().getMlbTeamId())){
                String stringToSendOut = teamScheduleStrings.get(subscription.getTeam().getMlbTeamId());
                //send out string in future method
            }
        }

        /*

        hashmap key = team, value = string of schedule info
        
        for every game in list, put into hashmap (key = team, value = add to string of schedule info)
        
        for every team sub:
            - get team and get team.mlbteamid
            
        
                
    
        */




       
    }

    @Scheduled(fixedRate = 15000)
    public void pollGames(){


        LocalDate todayUTC = LocalDate.now(ZoneOffset.UTC);
        String todayToString = todayUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String scheduleForWeekUrl = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&startDate=" + todayToString + "&endDate=";

        if(todayUTC.getDayOfWeek() == DayOfWeek.MONDAY){
            scheduleForWeekUrl = scheduleForWeekUrl + todayUTC.plusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=" + todayToString;
        log.info("Polling games for {}", todayToString);

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

            List<TeamSubscription> subscriptions;

            subscriptions = new ArrayList<>(teamSubscriptionRepository.findByTeam(awayTeam));
            subscriptions.addAll(teamSubscriptionRepository.findByTeam(homeTeam));

            if (subscriptions.isEmpty()) {
                log.info("No subscriptions for gamePk={}, skipping", game.getGamePk());
                continue;
            }

            if ("Final".equals(game.getStatus().getAbstractGameState())) {
                mlbLiveRetrievalService.processGameEnd(game.getGamePk(), subscriptions, lastGameState);
            } else {
                mlbLiveRetrievalService.processGame(game.getGamePk(), subscriptions, lastGameState, homeTeam, awayTeam);
            }
        }
    }
}
