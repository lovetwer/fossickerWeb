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

import java.util.*;

@RestController
@RequestMapping("/user/history")
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryRepository browseHistoryRepository;

    @Autowired
    private DealRepository dealRepository;

    @GetMapping
    public Result<Page<Map<String, Object>>> getBrowseHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "browseTime"));
        Page<BrowseHistory> historyPage = browseHistoryRepository.findByUserIdOrderByBrowseTimeDesc(userId, pageable);

        Page<Map<String, Object>> result = historyPage.map(history -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", history.getId());
            map.put("dealId", history.getDealId());
            map.put("browseTime", history.getBrowseTime());

            Optional<Deal> dealOpt = dealRepository.findById(history.getDealId());
            dealOpt.ifPresent(deal -> {
                map.put("title", deal.getTitle());
                map.put("platform", deal.getPlatform());
                map.put("images", deal.getImages());
                map.put("profit", deal.getProfit());
            });

            return map;
        });

        return Result.success(result);
    }

    @DeleteMapping("/{dealId}")
    public Result<Void> deleteBrowseHistory(
            @RequestHeader("Authorization") String token,
            @PathVariable String dealId) {

        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        browseHistoryRepository.deleteByUserIdAndDealId(userId, dealId);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clearBrowseHistory(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        browseHistoryRepository.deleteByUserId(userId);
        return Result.success("清空成功", null);
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
