package com.kobe.dinger.service;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kobe.dinger.repository.TeamRepository;
import com.kobe.dinger.repository.TeamSubscriptionRepository;

@Service
public class GamePollingService {
    private TeamSubscriptionRepository teamSubscriptionRepository;
    private TeamRepository teamRepository;
    private MlbLiveRetrievalService mlbLiveRetrievalService;
    private NotificationService notificationService;

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
        LocalDate today = LocalDate.now();
    }


}
