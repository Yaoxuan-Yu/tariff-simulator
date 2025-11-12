package com.example.insights.repository;

import com.example.insights.entity.UserSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, UUID> {
    List<UserSearch> findByUserId(String userId);
    List<UserSearch> findByCreatedAtAfter(LocalDateTime dateTime);
}

