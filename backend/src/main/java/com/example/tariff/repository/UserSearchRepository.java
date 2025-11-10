package com.example.tariff.repository;

import com.example.tariff.entity.UserSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserSearchRepository extends JpaRepository<UserSearch, UUID>{
    List<UserSearch> findByUserId(String userId);
    Page<UserSearch> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<UserSearch> findByCreatedAtAfter(LocalDateTime dateTime);
}
