package com.example.insights.repository;

import com.example.insights.entity.UserSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, Long> {
    List<UserSearch> findByUserId(String userId);
    List<UserSearch> findByCreatedAtAfter(LocalDateTime dateTime);
}

