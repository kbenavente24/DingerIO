package com.kobe.dinger.service;

import org.springframework.stereotype.Service;

import com.kobe.dinger.model.Team;
import com.kobe.dinger.model.TeamSubscription;
import com.kobe.dinger.model.User;
import com.kobe.dinger.repository.PlayerSubscriptionRepository;
import com.kobe.dinger.repository.SubscriptionRepository;
import com.kobe.dinger.repository.TeamRepository;
import com.kobe.dinger.repository.TeamSubscriptionRepository;
import com.kobe.dinger.repository.UserRepository;

@Service
public class SubscriptionService {
    
    private SubscriptionRepository subscriptionRepository;
    private TeamSubscriptionRepository teamSubscriptionRepository;
    private PlayerSubscriptionRepository playerSubscriptionRepository;
    private UserRepository userRepository;
    private TeamRepository teamRepository;


    public SubscriptionService(SubscriptionRepository subscriptionRepository, TeamSubscriptionRepository teamSubscriptionRepository, 
        PlayerSubscriptionRepository playerSubscriptionRepository, UserRepository userRepository, TeamRepository teamRepository){
            this.subscriptionRepository = subscriptionRepository;
            this.teamSubscriptionRepository = teamSubscriptionRepository;
            this.playerSubscriptionRepository = playerSubscriptionRepository;
            this.userRepository = userRepository;
            this.teamRepository = teamRepository;
    }

    public TeamSubscription createTeamSubscription(Integer userId, boolean notifyEveryInning, boolean notifyEndOfGame, Integer teamId){

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User does not exist"));

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team does not exist"));

        TeamSubscription teamSubscription = new TeamSubscription(user, notifyEveryInning, notifyEndOfGame, team);

        teamSubscriptionRepository.save(teamSubscription);
        return teamSubscription;
    }
}
