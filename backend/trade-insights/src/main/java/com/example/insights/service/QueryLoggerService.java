package com.example.insights.service;

import com.example.insights.entity.SearchType;
import com.example.insights.entity.UserSearch;
import com.example.insights.repository.UserSearchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

// service for logging user search queries for analytics
@Slf4j
@Service
public class QueryLoggerService {

    private static final String FILTER_QUERY = "query";
    private static final String FILTER_COUNTRY = "country";
    private static final String FILTER_PRODUCT = "product";
    private static final String FILTER_AGREEMENT_TYPE = "agreementType";
    private static final String QUERY_SEPARATOR = " | ";

    private final UserSearchRepository userSearchRepository;
    private final ObjectMapper objectMapper;

    public QueryLoggerService(UserSearchRepository userSearchRepository, ObjectMapper objectMapper) {
        this.userSearchRepository = userSearchRepository;
        this.objectMapper = objectMapper;
    }

    // log user search query to database for analytics (runs in separate transaction to avoid breaking main request)
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {InvalidDataAccessResourceUsageException.class, Exception.class})
    public void logSearch(String userId, SearchType searchType, Map<String, String> filters) {
        try {
            UserSearch search = new UserSearch();
            search.setUserId(userId);
            search.setSearchType(searchType);
            search.setSearchQuery(buildSearchQuery(filters));
            search.setFilters(objectMapper.writeValueAsString(filters));

            userSearchRepository.save(search);
        } catch (InvalidDataAccessResourceUsageException e) {
            // table doesn't exist - log at debug level and continue silently
            log.debug("Search logging table not available, skipping log for user {}", userId);
        } catch (Exception e) {
            // other database errors - log at warn level but don't break the request
            log.warn("Failed to log search for user {}: {}", userId, e.getMessage());
        }
    }

    // build human-readable search query string from filters
    private String buildSearchQuery(Map<String, String> filters) {
        StringBuilder sb = new StringBuilder();
        if (filters.containsKey(FILTER_QUERY)) {
            sb.append(filters.get(FILTER_QUERY));
        }
        if (filters.containsKey(FILTER_COUNTRY)) {
            sb.append(QUERY_SEPARATOR).append("Country: ").append(filters.get(FILTER_COUNTRY));
        }
        if (filters.containsKey(FILTER_PRODUCT)) {
            sb.append(QUERY_SEPARATOR).append("Product: ").append(filters.get(FILTER_PRODUCT));
        }
        if (filters.containsKey(FILTER_AGREEMENT_TYPE)) {
            sb.append(QUERY_SEPARATOR).append("Agreement: ").append(filters.get(FILTER_AGREEMENT_TYPE));
        }
        return sb.toString();
    }
}

