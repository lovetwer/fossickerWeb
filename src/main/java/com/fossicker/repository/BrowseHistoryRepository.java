package com.fossicker.repository;

import com.fossicker.entity.BrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, String> {

    Page<BrowseHistory> findByUserIdOrderByBrowseTimeDesc(String userId, Pageable pageable);

    Optional<BrowseHistory> findByUserIdAndDealId(String userId, String dealId);

    void deleteByUserIdAndDealId(String userId, String dealId);

    void deleteByUserId(String userId);
}
