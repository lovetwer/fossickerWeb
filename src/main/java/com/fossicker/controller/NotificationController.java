package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.Notification;
import com.fossicker.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public Result<Page<Notification>> getNotifications(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
        return Result.success(notifications);
    }

    @GetMapping("/unread")
    public Result<List<Notification>> getUnreadNotifications(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreateTimeDesc(userId);
        return Result.success(notifications);
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> getUnreadCount(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return Result.success(result);
    }

    @PutMapping("/{id}/read")
    public Result<Notification> markAsRead(@RequestHeader("Authorization") String token, @PathVariable String id) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        java.util.Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isEmpty()) {
            return Result.error("Notification not found");
        }

        Notification notification = notificationOpt.get();
        if (!userId.equals(notification.getUserId())) {
            return Result.error(403, "No permission");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return Result.success("已标记为已读", notification);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreateTimeDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);

        return Result.success("全部标记为已读", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@RequestHeader("Authorization") String token, @PathVariable String id) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        java.util.Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isEmpty()) {
            return Result.error("Notification not found");
        }

        Notification notification = notificationOpt.get();
        if (!userId.equals(notification.getUserId())) {
            return Result.error(403, "No permission");
        }

        notificationRepository.deleteById(id);
        return Result.success("删除成功", null);
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
