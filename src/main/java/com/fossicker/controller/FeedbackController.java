package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.Feedback;
import com.fossicker.entity.User;
import com.fossicker.repository.FeedbackRepository;
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
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Result<Feedback> submitFeedback(@RequestHeader("Authorization") String token,
                                           @RequestBody Feedback feedback) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Result.error("User not found");
        }

        User user = userOpt.get();

        feedback.setId(null);
        feedback.setUserId(userId);
        feedback.setUserNickname(user.getNickname());
        feedback.setStatus(0);
        feedback.setCreateTime(new Date());

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return Result.success("提交成功", savedFeedback);
    }

    @GetMapping("/my")
    public Result<List<Feedback>> getMyFeedback(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        List<Feedback> feedbackList = feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return Result.success(feedbackList);
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
