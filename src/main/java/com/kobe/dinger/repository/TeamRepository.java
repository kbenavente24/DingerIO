package com.kobe.dinger.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kobe.dinger.model.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    public Optional<Team> findByMlbTeamId(Integer mlbTeamId);

    public boolean existsByMlbTeamId(Integer mlbTeamId);

    public List<Team> findByTeamNameContainingIgnoreCase(String name);

}
