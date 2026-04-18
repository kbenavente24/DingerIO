package com.kobe.dinger.model;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subscriptionId;

    /*Self reminder: JPA/Hibernate stores the foreign key of user_id (subscription table) in the database, but in Java
    we hold reference to the actual User object. This is the point of Object Relational Mapping (ORM). That is, objects
    in Java, tables in the database.
    */
    @ManyToOne
    @JoinColumn(name = "user_id") // we use this annotation so that we can still name the column in this table ourselves 
    private User user;

    @CreationTimestamp
    private Instant createdAt;

    private boolean notifyEveryInning;

    private boolean notifyEndOfGame;

    public Subscription() {}

    public Integer getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Integer subscriptionId) { this.subscriptionId = subscriptionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isNotifyEveryInning() { return notifyEveryInning; }
    public void setNotifyEveryInning(boolean notifyEveryInning) { this.notifyEveryInning = notifyEveryInning; }

    public boolean isNotifyEndOfGame() { return notifyEndOfGame; }
    public void setNotifyEndOfGame(boolean notifyEndOfGame) { this.notifyEndOfGame = notifyEndOfGame; }
}
