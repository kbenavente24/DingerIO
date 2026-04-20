package com.kobe.dinger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kobe.dinger.model.Player;
import com.kobe.dinger.model.PlayerSubscription;

@Repository
public interface PlayerSubscriptionRepository extends JpaRepository<PlayerSubscription, Integer> {
    
    public List<PlayerSubscription> findByPlayer(Player player);

}
