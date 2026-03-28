package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.entity.Deal;
import com.fossicker.entity.User;
import com.fossicker.repository.DealRepository;
import com.fossicker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DealRepository dealRepository;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        Optional<User> user = userRepository.findById(userId);
        return user.map(Result::success).orElseGet(() -> Result.error("User not found"));
    }

    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody User user) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isEmpty()) {
            return Result.error("User not found");
        }
        
        User currentUser = existingUser.get();
        if (user.getNickname() != null) {
            currentUser.setNickname(user.getNickname());
        }
        if (user.getAvatar() != null) {
            currentUser.setAvatar(user.getAvatar());
        }
        if (user.getPhone() != null) {
            currentUser.setPhone(user.getPhone());
        }
        
        User updatedUser = userRepository.save(currentUser);
        return Result.success("Updated successfully", updatedUser);
    }

    @GetMapping("/deals")
    public Result<List<Deal>> getMyDeals(@RequestHeader("Authorization") String token) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }
        List<Deal> deals = dealRepository.findByPublisherIdOrderByPublishTimeDesc(userId);
        return Result.success(deals);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestHeader("Authorization") String token,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        String userId = parseToken(token);
        if (userId == null) {
            return Result.error(401, "Invalid token");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Result.error("User not found");
        }

        User user = userOpt.get();

        // 验证旧密码
        if (!passwordMatch(oldPassword, user.getPassword())) {
            return Result.error(400, "旧密码错误");
        }

        // 更新新密码
        user.setPassword(encryptPassword(newPassword));
        userRepository.save(user);

        return Result.success("密码修改成功", null);
    }

    private String parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 实现token验证和解析逻辑
        // 临时方案：直接返回token作为userId（需要配合AuthController的token生成逻辑）
        return token;
    }

    private String encryptPassword(String password) {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password);
    }

    private boolean passwordMatch(String inputPassword, String storedPassword) {
        return inputPassword != null && new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(inputPassword, storedPassword);
    }
}
