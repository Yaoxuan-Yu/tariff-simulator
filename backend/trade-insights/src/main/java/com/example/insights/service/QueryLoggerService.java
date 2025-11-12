package com.example.insights.service;

import com.example.insights.entity.SearchType;
import com.example.insights.entity.UserSearch;
import com.example.insights.repository.UserSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class QueryLoggerService {

    private final UserSearchRepository userSearchRepository;
    private final ObjectMapper objectMapper;

    public QueryLoggerService(UserSearchRepository userSearchRepository, ObjectMapper objectMapper) {
        this.userSearchRepository = userSearchRepository;
        this.objectMapper = objectMapper;
    }

    public void logSearch(String userId, SearchType searchType, Map<String, String> filters) {
        try {
            UserSearch search = new UserSearch();
            search.setUserId(userId);
            search.setSearchType(searchType);
            search.setSearchQuery(buildSearchQuery(filters));
            search.setFilters(objectMapper.writeValueAsString(filters));

            userSearchRepository.save(search);
        } catch (Exception e) {
            log.error("Failed to log search for user {}", userId, e);
        }
    }

    private String buildSearchQuery(Map<String, String> filters) {
        StringBuilder sb = new StringBuilder();
        if (filters.containsKey("query")) {
            sb.append(filters.get("query"));
        }
        if (filters.containsKey("country")) {
            sb.append(" | Country: ").append(filters.get("country"));
        }
        if (filters.containsKey("product")) {
            sb.append(" | Product: ").append(filters.get("product"));
        }
        if (filters.containsKey("agreementType")) {
            sb.append(" | Agreement: ").append(filters.get("agreementType"));
        }
        return sb.toString();
    }
}

