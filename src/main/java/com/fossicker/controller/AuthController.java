package com.fossicker.controller;

import com.fossicker.common.Result;
import com.fossicker.dto.LoginDTO;
import com.fossicker.dto.RegisterDTO;
import com.fossicker.entity.User;
import com.fossicker.repository.UserRepository;
import com.fossicker.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        if ("device".equals(dto.getLoginType())) {
            return loginByDevice(dto);
        } else if ("account".equals(dto.getLoginType())) {
            return loginByAccount(dto);
        }
        return Result.error("Invalid login type");
    }

    private Result<LoginVO> loginByDevice(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findByOpenId(dto.getOpenId());
        if (userOpt.isEmpty()) {
            return Result.error(404, "用户不存在");
        }

        User user = userOpt.get();
        String token = generateToken(user.getId());
        return Result.success(new LoginVO(token, user));
    }

    private Result<LoginVO> loginByAccount(LoginDTO dto) {
        String username = dto.getUsername() != null ? dto.getUsername() : dto.getNickname();
        Optional<User> userOpt = userRepository.findByNickname(username);
        if (userOpt.isEmpty() || !passwordMatch(dto.getPassword(), userOpt.get().getPassword())) {
            return Result.error(500, "账号或密码错误");
        }

        User user = userOpt.get();
        String token = generateToken(user.getId());
        return Result.success(new LoginVO(token, user));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody RegisterDTO dto) {
        if (userRepository.existsByNickname(dto.getNickname())) {
            return Result.error(409, "昵称已被使用");
        }

        User user = new User();
        user.setOpenId(dto.getOpenId());
        user.setNickname(dto.getNickname());
        user.setPassword(encryptPassword(dto.getPassword()));
        user.setDeviceModel(dto.getDeviceModel());
        user.setLevel(1);
        user.setPoints(0);
        user.setPublishCount(0);
        user.setCreateTime(new Date());

        userRepository.save(user);

        String token = generateToken(user.getId());
        return Result.success(new LoginVO(token, user));
    }

    private String generateToken(String userId) {
        // 直接使用 userId 作为 token，简化验证逻辑
        return userId;
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean passwordMatch(String inputPassword, String storedPassword) {
        return inputPassword != null && passwordEncoder.matches(inputPassword, storedPassword);
    }
}
