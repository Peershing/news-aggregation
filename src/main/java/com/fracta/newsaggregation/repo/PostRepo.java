package com.fracta.newsaggregation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fracta.newsaggregation.model.Post;

@Repository
public interface PostRepo extends JpaRepository<Post, Long>{

}
