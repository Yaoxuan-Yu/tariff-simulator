package com.example.tariff.service;

import com.example.tariff.entity.UserSearch;
import com.example.tariff.entity.SearchType;
import com.example.tariff.repository.UserSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;


@Slf4j
@Service
public class QueryLoggerService {
    @Autowired
    private UserSearchRepository userSearchRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public void logSearch(String userId, SearchType searchType, Map<String, String> filters) {
        try {
            UserSearch search = new UserSearch();
            search.setUserId(userId);
            search.setSearchType(searchType);
            search.setSearchQuery(buildSearchQuery(filters));
            search.setFilters(objectMapper.writeValueAsString(filters));
            
            userSearchRepository.save(search);
            log.info("Search logged for user: {}", userId);
        } catch (Exception e) {
            log.error("Error logging search", e);
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
        return sb.toString();
    }
}
