package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.BrowseHistory;
import com.fossicker.entity.Deal;
import com.fossicker.repository.BrowseHistoryRepository;
import com.fossicker.repository.DealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/deals")
public class DealController {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    @GetMapping
    public Result<Page<Deal>> getDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Integer status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishTime"));
        Page<Deal> deals;

        // 默认查询状态为1（正常）的数据
        Integer queryStatus = (status != null) ? status : 1;

        if (categoryName != null && !categoryName.isEmpty()) {
            deals = dealRepository.findByCategoryNameAndStatusOrderByPublishTimeDesc(categoryName, queryStatus, pageable);
        } else {
            deals = dealRepository.findByStatusOrderByPublishTimeDesc(queryStatus, pageable);
        }

        return Result.success(deals);
    }

    @GetMapping("/{id}")
    public Result<Deal> getDealById(@PathVariable String id,
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        Optional<Deal> deal = dealRepository.findById(id);
        if (deal.isEmpty()) {
            return Result.error("Deal not found");
        }

        // 记录浏览历史
        if (token != null && !token.isEmpty()) {
            String userId = parseToken(token);
            if (userId != null) {
                recordBrowseHistory(userId, id);
            }
        }

        return Result.success(deal.get());
    }

    private void recordBrowseHistory(String userId, String dealId) {
        Optional<BrowseHistory> existing = browseHistoryRepository.findByUserIdAndDealId(userId, dealId);
        if (existing.isPresent()) {
            BrowseHistory history = existing.get();
            history.setBrowseTime(new Date());
            browseHistoryRepository.save(history);
        } else {
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setDealId(dealId);
            browseHistoryRepository.save(history);
        }
    }

    @PostMapping
    public Result<Deal> createDeal(@RequestHeader("Authorization") String token, @RequestBody Deal deal) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        
        deal.setId(null);
        deal.setPublisherId(userId);
        deal.setStatus(0);
        deal.setPublishTime(new Date());
        Deal savedDeal = dealRepository.save(deal);
        return Result.success("Created successfully", savedDeal);
    }

    @PutMapping("/{id}")
    public Result<Deal> updateDeal(@RequestHeader("Authorization") String token, @PathVariable String id, @RequestBody Deal deal) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        
        Optional<Deal> existingDeal = dealRepository.findById(id);
        if (existingDeal.isEmpty()) {
            return Result.error("Deal not found");
        }
        
        Deal currentDeal = existingDeal.get();
        // 检查是否是发布者
        if (!userId.equals(currentDeal.getPublisherId())) {
            return Result.error(403, "No permission");
        }
        
        deal.setId(id);
        deal.setPublisherId(userId);
        Deal updatedDeal = dealRepository.save(deal);
        return Result.success("Updated successfully", updatedDeal);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDeal(@RequestHeader("Authorization") String token, @PathVariable String id) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        
        Optional<Deal> deal = dealRepository.findById(id);
        if (deal.isEmpty()) {
            return Result.error("Deal not found");
        }
        
        Deal existingDeal = deal.get();
        // 检查是否是发布者
        if (!userId.equals(existingDeal.getPublisherId())) {
            return Result.error(403, "No permission");
        }
        
        existingDeal.setStatus(2);
        dealRepository.save(existingDeal);
        return Result.success("Deleted successfully", null);
    }

    @GetMapping("/search")
    public Result<Page<Deal>> searchDeals(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchType) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishTime"));
        Page<Deal> deals;

        if ("title".equals(searchType)) {
            deals = dealRepository.findByTitleContainingAndStatus(keyword, 1, pageable);
        } else if ("content".equals(searchType)) {
            deals = dealRepository.findByContentContainingAndStatus(keyword, 1, pageable);
        } else {
            deals = dealRepository.findByTitleContainingOrContentContainingAndStatus(keyword, keyword, 1, pageable);
        }

        return Result.success(deals);
    }
    
    private String parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }
}
