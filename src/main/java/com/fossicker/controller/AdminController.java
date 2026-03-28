package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.Deal;
import com.fossicker.entity.Feedback;
import com.fossicker.entity.Notification;
import com.fossicker.entity.User;
import com.fossicker.repository.DealRepository;
import com.fossicker.repository.FeedbackRepository;
import com.fossicker.repository.NotificationRepository;
import com.fossicker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @GetMapping("/deals/pending")
    public Result<Page<Deal>> getPendingDeals(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishTime"));
        Page<Deal> deals = dealRepository.findByStatusOrderByPublishTimeDesc(0, pageable);
        return Result.success(deals);
    }

    @PutMapping("/deals/{id}/approve")
    public Result<Deal> approveDeal(@RequestHeader("Authorization") String token, @PathVariable String id) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Optional<Deal> dealOpt = dealRepository.findById(id);
        if (dealOpt.isEmpty()) {
            return Result.error("Deal not found");
        }

        Deal deal = dealOpt.get();
        deal.setStatus(1);
        dealRepository.save(deal);

        // 发送审核通过通知
        sendNotification(deal.getPublisherId(), "审核通过", "您的羊毛「" + deal.getTitle() + "」已通过审核", "audit_approve", deal.getId());

        return Result.success("审核通过", deal);
    }

    @PutMapping("/deals/{id}/reject")
    public Result<Deal> rejectDeal(@RequestHeader("Authorization") String token, @PathVariable String id) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Optional<Deal> dealOpt = dealRepository.findById(id);
        if (dealOpt.isEmpty()) {
            return Result.error("Deal not found");
        }

        Deal deal = dealOpt.get();
        deal.setStatus(2);
        dealRepository.save(deal);

        // 发送审核拒绝通知
        sendNotification(deal.getPublisherId(), "审核未通过", "您的羊毛「" + deal.getTitle() + "」未通过审核", "audit_reject", deal.getId());

        return Result.success("已拒绝", deal);
    }

    @PutMapping("/deals/{id}/offline")
    public Result<Deal> offlineDeal(@RequestHeader("Authorization") String token, @PathVariable String id) {
        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Optional<Deal> dealOpt = dealRepository.findById(id);
        if (dealOpt.isEmpty()) {
            return Result.error("Deal not found");
        }

        Deal deal = dealOpt.get();
        deal.setStatus(2);
        dealRepository.save(deal);

        // 发送下架通知
        sendNotification(deal.getPublisherId(), "内容下架", "您的羊毛「" + deal.getTitle() + "」已被管理员下架", "offline", deal.getId());

        return Result.success("已下架", deal);
    }

    @PostMapping("/notifications")
    public Result<Void> sendSystemNotification(
            @RequestHeader("Authorization") String token,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String userId) {

        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        if (userId != null && !userId.isEmpty()) {
            sendNotification(userId, title, content, "system", null);
        } else {
            sendNotificationToAll(title, content);
        }

        return Result.success("发送成功", null);
    }

    @GetMapping("/users/search")
    public Result<Page<User>> searchUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> users = userRepository.findByNicknameContaining(keyword, pageable);
        return Result.success(users);
    }

    @GetMapping("/users")
    public Result<Page<User>> getAllUsers(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<User> users = userRepository.findAll(pageable);
        return Result.success(users);
    }

    @GetMapping("/feedback")
    public Result<Page<Feedback>> getFeedbackList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Feedback> feedbackPage;

        if (status != null) {
            feedbackPage = feedbackRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        } else {
            feedbackPage = feedbackRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        return Result.success(feedbackPage);
    }

    @PutMapping("/feedback/{id}/reply")
    public Result<Feedback> replyFeedback(
            @RequestHeader("Authorization") String token,
            @PathVariable String id,
            @RequestParam String reply) {

        if (!isAdmin(token)) {
            return Result.error(403, "无权限");
        }

        String adminId = parseToken(token);
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(id);
        if (feedbackOpt.isEmpty()) {
            return Result.error("Feedback not found");
        }

        Feedback feedback = feedbackOpt.get();
        feedback.setReply(reply);
        feedback.setReplyBy(adminId);
        feedback.setReplyTime(new Date());
        feedback.setStatus(1);

        feedbackRepository.save(feedback);

        feedback.setReply(reply);
        feedback.setReplyBy(adminId);
        feedback.setReplyTime(new Date());
        feedback.setStatus(1);

        feedbackRepository.save(feedback);

        return Result.success("回复成功", feedback);
    }

    private void sendNotificationToAll(String title, String content) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("system");
            notification.setDealId(null);
            notificationRepository.save(notification);
        }
    }

    private boolean isAdmin(String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.isPresent() && "admin".equals(userOpt.get().getRole());
    }

    private void sendNotification(String userId, String title, String content, String type, String dealId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setDealId(dealId);
        notificationRepository.save(notification);
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
