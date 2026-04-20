package com.kobe.dinger.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_subscription")
public class PlayerSubscription extends Subscription {

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    public PlayerSubscription(User user, boolean notifyEveryInning, boolean notifyEndOfGame, Player player) {

        super(user, notifyEveryInning, notifyEndOfGame);
        this.player = player;

    }

    public Player getPlayer(){
        return this.player;
    }

    public void setPlayer(Player player){
        this.player = player;
    }

}
