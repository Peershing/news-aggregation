package com.fracta.newsaggregation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fracta.newsaggregation.model.Vote;

@Repository
public interface VoteRepo extends JpaRepository<Vote, Long> {

}
