package com.kobe.dinger.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.kobe.dinger.model.Subscription;
import com.kobe.dinger.model.User;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    
    public List<Subscription> findByUser(User user);

}
