package com.kobe.dinger.DTOs.request;

public class SubscriptionRequest {
    
    private Integer userId;
    private Integer teamId;
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public Integer getTeamId() {
        return teamId;
    }
    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }



}
